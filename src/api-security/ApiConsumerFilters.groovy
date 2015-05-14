package grails.plugin.apisecurity

import grails.web.Action

class ApiConsumerFilters {

    def grailsApplication
    def apiConsumerService
    def springSecurityService

    def filters = {
        securedApi( controller:"*", action:"*" ) {
            before = {

                //log.error "WARNING: securedApi is commented out"
                //return true

                def annotation = annotationFor(controllerName, actionName)
                if (annotation) {
                    def resource = request.forwardURI
                    def accessToken = obtainAccessToken(request)
                    // NOTE: don't log the accessToken!  That's a huge security hole open to anyone with access to the log files.
                    //log.debug "resource: ${resource}, accessToken: ${accessToken}"
                    log.debug "resource: ${resource}"

                    def validatedApiConsumer = apiConsumerService.validateResource(resource, accessToken)
                    if (validatedApiConsumer) {
                        // put the apiConsumer on the request...
                        request.apiConsumer = validatedApiConsumer
                        //log.info "access granted: ${resource}, accessToken: ${accessToken}"
                        log.info "access granted: ${resource}"
                        return true

                    } else {
                        //log.warn "access denied: ${resource}, accessToken: ${accessToken}"
                        log.warn "access denied: ${resource}"
                        render status: 401, text: "Unauthorized"
                        return false
                    }
                } else {
                    return true
                }
            }

        }
    }

    private static final String AUTHORIZATION_HEADER_VAR = "authorization"
    private static final String AUTHORIZATION_HEADER_VALUE_PREFIX = "bearer "

    private String obtainAccessToken(def request) {
        def header = request.getHeader(AUTHORIZATION_HEADER_VAR)
        if (header?.toLowerCase()?.startsWith(AUTHORIZATION_HEADER_VALUE_PREFIX)) {
            return header.substring(AUTHORIZATION_HEADER_VALUE_PREFIX.length())
        } else {
            return null
        }
    }

    /**
     *  The annotation map is a map of maps used to lookup controller/action annotation data.  It looks
     *  something like...
     *
     *  [
     *      ctrlName1: [
     *          actionName1: @...SecuredApi(...),
     *          actionName2: @...SecuredApi(...),
     *          ...
     *      ],
     *      ...
     *  ]
     */
    private static def _annotationMap = [:]

    private def annotationFor(String controllerName, String actionName) {
        if (!_annotationMap[controllerName]) {
            cacheAnnotationsFor(controllerName)
        }
        //println _annotationMap
        _annotationMap[controllerName][actionName]
    }

    private void cacheAnnotationsFor(String controllerName) {
        def map = [:]
        def artefact = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName)
        def applicationContext = grailsApplication.mainContext
        if (artefact) {
            def bean = applicationContext.getBean(artefact.clazz.name)
            bean?.class.methods.each { method ->
                if (method.getAnnotation(Action) && method.getAnnotation(SecuredApi)) {
                    map[method.name] = method.getAnnotation(SecuredApi)
                }
            }
        }
        _annotationMap[controllerName] = map
    }

}

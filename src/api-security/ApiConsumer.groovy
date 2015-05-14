package grails.plugin.apisecurity

import xxxxx.Tenant

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes=["id"])
class ApiConsumer implements Comparable {

    String id

    static belongsTo = [ tenant: Tenant ]

    String activationName
    String activationCode
    String activationKey // a shadow field that holds activationName + activationCode
    Date activationExpirationDate = null
    Boolean allowMultipleActivations = true

    List<String> scope

    String accessCode
    Date activationDate
    Date expirationDate
    Date lastUsed

    Boolean enabled = true
    Boolean locked = false

    Date dateCreated
    Date lastUpdated

    static constraints = {
        activationName blank: false, minSize: 6, maxSize: 50
        activationCode blank: false, minSize: 6, maxSize: 50
        activationKey unique: true
        activationExpirationDate nullable: true

        scope nullable: true

        // NOTE: when not null, accessCode must be unique.  Not a problem as long as they are generated as UUID values!
        accessCode blank: false, nullable: true, minSize: 8, maxSize: 250

        activationDate nullable: true
        expirationDate nullable: true
        lastUsed nullable: true
    }

    def updateActivationKey() {
        activationKey = "${activationName}${activationCode}"
    }
    def beforeValidate() { updateActivationKey() }
    def beforeInsert() { updateActivationKey() }
    def beforeUpdate() { updateActivationKey() }

    Boolean isActivatable(Date asOfDate = null) {
        (
            (
                !isActive(asOfDate)
                || allowMultipleActivations
            )
            && !isActivationExpired(asOfDate)
        )
    }

    Boolean isActivationExpired(Date asOfDate = null) {
        if (!activationExpirationDate) {
            return false
        } else {
            if (!asOfDate) {
                asOfDate = new Date()
            }
            return (activationExpirationDate < asOfDate)
        }
    }

    Boolean isActive(Date asOfDate = null) {
        if (!asOfDate) {
            asOfDate = new Date()
        }

        (
            enabled
            && !isExpired(asOfDate)
            && !locked
            && activationDate
            && (activationDate <= asOfDate)
        )
    }

    Boolean isExpired(Date asOfDate = null) {
        if (!expirationDate) {
            return false
        } else {
            if (!asOfDate) {
                asOfDate = new Date()
            }
            return (expirationDate < asOfDate)
        }
    }

    boolean validateResource(String resource) {
        if (!resource?.trim()) {
            return false
        } else if (!isActive()) {
            return false
        } else if (!scope) {
            return false
        } else {
            return scope.find { pattern -> matchUriPattern(resource, pattern) }
        }
    }

    private boolean matchUriPattern(String resource, String pattern) {

        boolean match = false

        def resourcePrefix = resource
        if (resourcePrefix.endsWith("/")) { // don't care about trailing slashes...
            resourcePrefix = resourcePrefix.substring(0, resourcePrefix.length() - 1)
        }

        if (pattern.endsWith("**")) { // wildcard pattern...
            def patternPrefix = pattern.substring(0, pattern.length() - 2)
            if (patternPrefix.endsWith("/")) { // don't care about trailing slashes...
                patternPrefix = patternPrefix.substring(0, patternPrefix.length() - 1)
            }
            match = resourcePrefix.startsWith(patternPrefix)

        } else { // absolute pattern...
            if (pattern.endsWith("/")) { // don't care about trailing slashes...
                match = (resourcePrefix == pattern.substring(0, pattern.length() - 1))
            } else {
                match = (resourcePrefix == pattern)
            }
        }

        //println "matchUriPattern(${resource}, ${pattern}) --> ${match}"
        return match
    }

    int compareTo(that) {
        (this.activationKey <=> that.activationKey)
    }

    String toString() {
        "${activationKey}"
    }

}

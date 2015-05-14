package grails.plugin.apisecurity

import grails.buildtestdata.mixin.Build

import spock.lang.Specification

@TestFor(ApiConsumerService)
@Build([ApiConsumer])
class ApiConsumerServiceSpec extends Specification {

    def "grantAuthorization(activationKey)"() {
        given:
            def apiConsumer = ApiConsumer.build()
            def activationKey = apiConsumer.activationKey
            def badActivationKey = "xyzzy"
        expect:
            activationKey != badActivationKey
            !service.grantAuthorization(badActivationKey)
            service.grantAuthorization(activationKey)
    }

    def "validateResource(resource, accessCode)"() {
        given:
            def apiConsumer = ApiConsumer.build(scope: ["/foo/bar/**"])
        expect:
            !service.validateResource("/foo", apiConsumer.accessCode)
            !service.validateResource("/foo/bar", apiConsumer.accessCode)
        when:
            service.activate(apiConsumer)
        then:
            !service.validateResource("/foo", apiConsumer.accessCode)
            service.validateResource("/foo/bar", apiConsumer.accessCode) == apiConsumer
            service.validateResource("/foo/bar/", apiConsumer.accessCode) == apiConsumer
    }

    def "activate(apiConsumer)"() {
    }

    def "generateActivationCode()"() {
        given:
            def code1 = service.generateActivationCode()
            def code2 = service.generateActivationCode()
        expect:
            code1.length() == 6
            code2.length() == 6
            code1 != code2
    }

    def "generateAccessCode()"() {
        given:
            def code1 = service.generateAccessCode()
            def code2 = service.generateAccessCode()
        expect:
            code1.length() == 36 // accessCodes are UUIDs
            code2.length() == 36
            code1 != code2
    }

}

package grails.plugin.apisecurity

import grails.buildtestdata.mixin.Build

import grails.test.mixin.TestFor

import spock.lang.Specification
import spock.lang.Unroll

@TestFor(ApiConsumer)
//@Build([Project])
class ApiConsumerSpec extends Specification {

    static DomainTestUtil testUtil = new DomainTestUtil()

    def setup() {
        // mock an apiConsumer with some proprties set for checking unique constraints
        mockForConstraintsTests(
            ApiConsumer,
            [ new ApiConsumer(
                activationName: "activationName-1",
                activationCode: "activationCode-1",
                accessCode: "accessCode-1"
                )
            ]
        )
    }

    @Unroll("#field : #error with value: #value.")
    def "constraints"() {
        when:
           def obj = new ApiConsumer("$field": value)

        then:
            testUtil.validateConstraints(obj, field, error)

        where:
            field             | error       | value

            "activationName"  | "nullable"  | null
            "activationName"  | "nullable"  | "" // grails-2.3 binds empty strings to null.
            //"activationName"  | "unique"    | "activationName-1"
            "activationName"  | "valid"     | "activationName-2"

            "activationName"  | "minSize"  | testUtil.stringWithLength(5)
            "activationName"  | "valid"    | testUtil.stringWithLength(6)
            "activationName"  | "valid"     | testUtil.stringWithLength(50)
            "activationName"  | "maxSize"   | testUtil.stringWithLength(51)

            "activationCode"  | "nullable"  | null
            "activationCode"  | "nullable"  | "" // grails-2.3 binds empty strings to null.
            //"activationCode"  | "unique"    | "activationCode-1"
            "activationCode"  | "valid"     | "activationCode-2"

            "activationCode"  | "minSize"  | testUtil.stringWithLength(5)
            "activationCode"  | "valid"    | testUtil.stringWithLength(6)
            "activationCode"  | "valid"     | testUtil.stringWithLength(50)
            "activationCode"  | "maxSize"   | testUtil.stringWithLength(51)

            "accessCode"  | "valid"    | null
            "accessCode"  | "valid"    | "" // grails-2.3 binds empty strings to null.
            "accessCode"  | "valid"    | "accessCode-1" // passes 'cause we've removed unique constraint to allow multiple consumers with accessCode:null
            "accessCode"  | "valid"    | "accessCode-2"

            "accessCode"  | "minSize"  | testUtil.stringWithLength(7)
            "accessCode"  | "valid"    | testUtil.stringWithLength(8)
            "accessCode"  | "valid"    | testUtil.stringWithLength(250)
            "accessCode"  | "maxSize"  | testUtil.stringWithLength(251)
    }

    @Unroll("isActivationExpired() when asOfDate: #asOfDate, activationExpirationDate: #activationExpirationDate")
    void "isActivationExpired()"() {
        given:
            def consumer = new ApiConsumer(activationExpirationDate: activationExpirationDate)
            def asOfDate = new Date("2014/4/1")
        expect:
            consumer.isActivationExpired(asOfDate) == isActivationExpired
        where:
            isActivationExpired | activationExpirationDate
            true                | new Date("2014/3/31")
            false               | new Date("2014/4/1")
            false               | new Date("2014/4/2")
            false               | null
    }

    void "isActivatable"() {
        when:
            def asOfDate = new Date("2014/4/1")
            def consumer = new ApiConsumer()
        then:
            !consumer.isActive(asOfDate)
            consumer.isActivatable(asOfDate)
    }

    void "isActivatable when allowMultipleActivations == true"() {
        when:
            def asOfDate = new Date("2014/4/1")
            def consumer = new ApiConsumer()
            consumer.allowMultipleActivations = true
        then:
            !consumer.isActive(asOfDate)
            consumer.isActivatable(asOfDate)

        when:
            consumer.activationDate = new Date("2014/1/1")
            consumer.isActive(asOfDate)
        then: // the consumer can be activated again
            consumer.isActivatable(asOfDate)
    }

    void "isActivatable when allowMultipleActivations == false"() {
        when:
            def asOfDate = new Date("2014/4/1")
            def consumer = new ApiConsumer()
            consumer.allowMultipleActivations = false
        then:
            !consumer.isActive(asOfDate)
            consumer.isActivatable(asOfDate)

        when:
            consumer.activationDate = new Date("2014/1/1")
        then: // the consumer cannot be activated again
            consumer.isActive(asOfDate)
            !consumer.isActivatable(asOfDate)
    }

    @Unroll("isActive() when enabled: #enabled, expirationDate: #expirationDate, locked: #locked, activationDate: #activationDate")
    void "isActive()"() {
        given:
            def consumer = new ApiConsumer(enabled: enabled, expirationDate: expirationDate, locked: locked, activationDate: activationDate)
            def asOfDate = new Date("2014/4/1")
        expect:
            consumer.isActive(asOfDate) == isActive
        where:
            isActive | activationDate       | locked | expirationDate       | enabled

            // never active when there is no activationDate...
            false    | null                 | false  | null                 | false
            false    | null                 | false  | null                 | true
            false    | null                 | false  | new Date("2014/3/1") | false
            false    | null                 | false  | new Date("2014/3/1") | true
            false    | null                 | false  | new Date("2014/4/1") | false
            false    | null                 | false  | new Date("2014/4/1") | true
            false    | null                 | false  | new Date("2014/5/1") | false
            false    | null                 | false  | new Date("2014/5/1") | true
            false    | null                 | true   | null                 | false
            false    | null                 | true   | null                 | true
            false    | null                 | true   | new Date("2014/3/1") | false
            false    | null                 | true   | new Date("2014/3/1") | true
            false    | null                 | true   | new Date("2014/4/1") | false
            false    | null                 | true   | new Date("2014/4/1") | true
            false    | null                 | true   | new Date("2014/5/1") | false
            false    | null                 | true   | new Date("2014/5/1") | true

            // never activationDate when locked or not enabled...
            false    | new Date("2014/3/1") | false  | null                 | false
            false    | new Date("2014/3/1") | false  | new Date("2014/3/1") | false
            false    | new Date("2014/3/1") | false  | new Date("2014/4/1") | false
            false    | new Date("2014/3/1") | false  | new Date("2014/5/1") | false
            false    | new Date("2014/3/1") | true   | null                 | false
            false    | new Date("2014/3/1") | true   | null                 | true
            false    | new Date("2014/3/1") | true   | new Date("2014/3/1") | false
            false    | new Date("2014/3/1") | true   | new Date("2014/3/1") | true
            false    | new Date("2014/3/1") | true   | new Date("2014/4/1") | false
            false    | new Date("2014/3/1") | true   | new Date("2014/4/1") | true
            false    | new Date("2014/3/1") | true   | new Date("2014/5/1") | false
            false    | new Date("2014/3/1") | true   | new Date("2014/5/1") | true
            false    | new Date("2014/4/1") | false  | null                 | false
            false    | new Date("2014/4/1") | false  | new Date("2014/3/1") | false
            false    | new Date("2014/4/1") | false  | new Date("2014/4/1") | false
            false    | new Date("2014/4/1") | false  | new Date("2014/5/1") | false
            false    | new Date("2014/4/1") | true   | null                 | false
            false    | new Date("2014/4/1") | true   | null                 | true
            false    | new Date("2014/4/1") | true   | new Date("2014/3/1") | false
            false    | new Date("2014/4/1") | true   | new Date("2014/3/1") | true
            false    | new Date("2014/4/1") | true   | new Date("2014/4/1") | false
            false    | new Date("2014/4/1") | true   | new Date("2014/4/1") | true
            false    | new Date("2014/4/1") | true   | new Date("2014/5/1") | false
            false    | new Date("2014/4/1") | true   | new Date("2014/5/1") | true

            // never active when the activationDate is in the future...
            false    | new Date("2014/5/1") | false  | null                 | false
            false    | new Date("2014/5/1") | false  | null                 | true
            false    | new Date("2014/5/1") | false  | new Date("2014/3/1") | false
            false    | new Date("2014/5/1") | false  | new Date("2014/3/1") | true
            false    | new Date("2014/5/1") | false  | new Date("2014/4/1") | false
            false    | new Date("2014/5/1") | false  | new Date("2014/4/1") | true
            false    | new Date("2014/5/1") | false  | new Date("2014/5/1") | false
            false    | new Date("2014/5/1") | false  | new Date("2014/5/1") | true
            false    | new Date("2014/5/1") | true   | null                 | false
            false    | new Date("2014/5/1") | true   | null                 | true
            false    | new Date("2014/5/1") | true   | new Date("2014/3/1") | false
            false    | new Date("2014/5/1") | true   | new Date("2014/3/1") | true
            false    | new Date("2014/5/1") | true   | new Date("2014/4/1") | false
            false    | new Date("2014/5/1") | true   | new Date("2014/4/1") | true
            false    | new Date("2014/5/1") | true   | new Date("2014/5/1") | false
            false    | new Date("2014/5/1") | true   | new Date("2014/5/1") | true

            // when the activationDate is in the past...
            true     | new Date("2014/3/1") | false  | null                 | true
            false    | new Date("2014/3/1") | false  | new Date("2014/3/1") | true // expired
            true     | new Date("2014/3/1") | false  | new Date("2014/4/1") | true
            true     | new Date("2014/3/1") | false  | new Date("2014/5/1") | true

            // when the activationDate is asOfDate...
            true     | new Date("2014/4/1") | false  | null                 | true
            false    | new Date("2014/4/1") | false  | new Date("2014/3/1") | true // expired
            true     | new Date("2014/4/1") | false  | new Date("2014/4/1") | true
            true     | new Date("2014/4/1") | false  | new Date("2014/5/1") | true
    }

    @Unroll("isExpired() when expirationDate: #expirationDate")
    void "isExpired()"() {
        given:
            def consumer = new ApiConsumer(expirationDate: expirationDate)
            def asOfDate = new Date("2014/4/1")
        expect:
            consumer.isExpired(asOfDate) == isExpired
        where:
            isExpired | expirationDate
            true      | new Date("2014/3/31")
            false     | new Date("2014/4/1")
            false     | new Date("2014/4/2")
    }

    @Unroll("validateResource(#resource) against scope: #scope")
    void "validateResource"() {
        given:
        def activationDate = new Date("2000/4/1") // choose a date way in the past to ensure the consumer is active
            def consumer = new ApiConsumer(activationDate: activationDate, scope: scope)
        expect:
            consumer.isActive()
            consumer.validateResource(resource) == valid
        where:
            scope                   | resource   | valid

            null                    | " "        | false
            null                    | ""         | false
            null                    | null       | false

            null                    | "/"        | false
            []                      | "/"        | false
            ["/"]                   | "/"        | true
            ["/api"]                | "/"        | false

            ["/"]                   | "/api"     | false
            ["api"]                 | "/api"     | false
            ["/api"]                | "/api"     | true

            ["/api"]                | "/api"     | true
            ["/api"]                | "/api/"    | true

            ["/api/" ]              | "/api"     | true
            ["/api/"]               | "/api/"    | true

            ["/api"]                | "/api/b"   | false
            ["/api/**"]             | "/api/b"   | true
            ["/api/**"]             | "/api/b/c" | true

            ["/api/a", "/api/b/**"] | "/api/a"   | true
            ["/api/a", "/api/b/**"] | "/api/b"   | true
            ["/api/a", "/api/b/**"] | "/api/c"   | false
            ["/api/a", "/api/b/**"] | "/api/b/e" | true
    }

}

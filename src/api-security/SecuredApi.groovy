package grails.plugin.apisecurity

import java.lang.annotation.*

/**
 *  Marker annotation (no args) for controller actions, defining whether or not an ApiConsumer accessCode is
 *  required for access to the action.  Checking the accessCode is delegated to the apiConsumerService by the
 *  apiConsumerFilters.
 *
 *  See ApiConsumerFilters.groovy.
 *  See ApiConsumerService.groovy.
 */
@Target([ElementType.FIELD, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@interface SecuredApi {
}

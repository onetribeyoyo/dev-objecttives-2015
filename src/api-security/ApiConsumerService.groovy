package grails.plugin.apisecurity

import org.apache.commons.lang.RandomStringUtils

/**
 *  Provides an impelementation of the methods needed to secure
 *  resources with an OATH 2 style process.
 */
class ApiConsumerService {

    static transactional = "mongo"

    /**
     *  Returns an accessCode, or null when denied.
     */
    String grantAuthorization(String activationKey) {
        ApiConsumer apiConsumer = ApiConsumer.findByActivationKey(activationKey)
        String granted = null

        if (apiConsumer && apiConsumer.isActivatable()) {
            activate(apiConsumer)
            granted = apiConsumer.accessCode
        }

        log.info "grantAuthorization(${activationKey}) --> ${granted}"
        return granted
    }

    /**
     *  Returns a valid ApiConsumer instance when the accessCode is valid, otherwise returns null.
     */
    ApiConsumer validateResource(String resource, String accessCode) {
        ApiConsumer apiConsumer = ApiConsumer.findByAccessCode(accessCode)
        if (!apiConsumer) {
            return null
        } else if (apiConsumer.validateResource(resource)) {
            return apiConsumer
        } else {
            return null
        }
    }

    String generateActivationCode(length = 6) {
        RandomStringUtils.randomAlphanumeric(length)
    }

    String generateUniqueActivationCode(length = 6) {
        def code = generateActivationCode(length)

        def limit = 1
        while(limit++ < 21 && ApiConsumer.findByActivationCode(code)) {
            code = generateActivationCode(length)

        }

        if(limit > 2) {
            log.info("APIConsumerService.generateUniqueActivationCode() is getting inefficient as it took $limit attempts")
        }

        return code
    }

    private void activate(ApiConsumer apiConsumer) {
        if (!apiConsumer.accessCode) {
            apiConsumer.accessCode = generateAccessCode()
        }
        apiConsumer.activationDate = new Date()
        apiConsumer.lastUsed = apiConsumer.activationDate
        apiConsumer.save()
    }

    private void deactivate(ApiConsumer apiConsumer) {
        apiConsumer.enabled = false
        apiConsumer.lastUsed = apiConsumer.activationDate
        apiConsumer.save()
    }

    private String generateAccessCode() {
        UUID.randomUUID()
    }

}

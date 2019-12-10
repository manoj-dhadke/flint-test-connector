/*************************************************************************
 * 
 * INFIVERVE TECHNOLOGIES PTE LIMITED CONFIDENTIAL
 * __________________
 * 
 * (C) INFIVERVE TECHNOLOGIES PTE LIMITED, SINGAPORE
 * All Rights Reserved.
 * Product / Project: Flint IT Automation Platform
 * NOTICE:  All information contained herein is, and remains
 * the property of INFIVERVE TECHNOLOGIES PTE LIMITED.
 * The intellectual and technical concepts contained
 * herein are proprietary to INFIVERVE TECHNOLOGIES PTE LIMITED.
 * Dissemination of this information or any form of reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from INFIVERVE TECHNOLOGIES PTE LIMITED, SINGAPORE.
 */
package com.infiverve.flint.connector.test;

import com.infiverve.flint.exception.EncryptionException;
import com.infiverve.flint.sdk.connectors.FlintConnectorBase;
import com.infiverve.flint.sdk.connectors.FlintConnectorRequest;
import com.infiverve.flint.sdk.connectors.exceptions.FlintConnectorException;
import com.infiverve.flint.sdk.logger.FlintLogger;
import io.vertx.core.json.JsonObject;

public class TestConnectorService extends FlintConnectorBase {

    private String username = null;
    private String password = null;
    private static String name = null;

    /**
     * Starts the connector. Configuration Parameters are available here.
     *
     * @param config - JSON object containing connector configuration data
     * @throws FlintConnectorException
     */
    @Override
    protected void enable(JsonObject config) throws FlintConnectorException {

        FlintLogger.PLATFORM_LOGGER.info("Test connector module started");

        /**
         * process connector configuration data
         *
         */
        try {

            /**
             * name of the connector
             */
            name = config.getString(STATIC.FIELD.NAME);

            /**
             * retrieve Connector configuration parameters example: username and
             * password are obtained from 'config' JSON
             */
            username = config.getString(STATIC.FIELD.USERNAME);
            password = config.getString(STATIC.FIELD.PASSWORD);

            /**
             * Usage of Flint Loggers
             */
            FlintLogger.PLATFORM_LOGGER.info("Connector is deployed with name :" + name
                    + "\n" + "username :" + username);

            /**
             * Access Flint Loggers like below PLATFORM_LOGGER = connector
             * enabling/disabling related logs JOB_LOGGER = connector action
             * performance related logs
             */
            FlintLogger.PLATFORM_LOGGER.debug("PLATFORM_LOGGER");
            FlintLogger.JOB_LOGGER.debug("JOB_LOGGER");

        } catch (Exception e) {
            throw new FlintConnectorException(-1, e.getLocalizedMessage());
        }

    }

    /**
     * Stop's the connector.
     *
     * @throws FlintConnectorException
     */
    @Override
    protected void disable() throws FlintConnectorException {

        /**
         * Your clean up code must go here will be called while stopping flint
         */
        FlintLogger.PLATFORM_LOGGER.info("Test connector module stopped");
    }

    /**
     * Processes the job request being submitted and performs the connector
     * action.
     *
     * @param request - connector request which contains all request data
     */
    @Override
    protected void onRequest(FlintConnectorRequest request) {

        FlintLogger.JOB_LOGGER.info("Request received inside onRequest method..");
        final JsonObject requestJson;
        final JsonObject response = new JsonObject();
        String username = null, password = null, jobId = null;

        try {
            //job-id is a unique identifier of the request being submitted
            jobId = request.getJobID();
            if (jobId == null) {
                jobId = "NO JOBID";
            }

            if (request.getRequestJson() != null) {
                //connector request parameters
                final JsonObject receivedBody = request.getRequestJson();

                //name of the action connector is going to perform
                if (receivedBody.containsKey(STATIC.FIELD.ACTION) && receivedBody.getString(STATIC.FIELD.ACTION).equals(STATIC.FIELD.AUTHENTICATE)) {

                    /**
                     * checks if username is passed as a request parameter
                     */
                    if (receivedBody.containsKey(STATIC.FIELD.USERNAME)) {
                        username = receivedBody.getString(STATIC.FIELD.USERNAME);

                        /**
                         * append job-id in every log statement for
                         * identification of request
                         */
                        FlintLogger.JOB_LOGGER.debug(jobId + " Getting username from request " + username);
                    } else {
                        /**
                         * if username is not passed as a request parameter
                         * username from config parameter will be used
                         */
                        username = this.username;
                        FlintLogger.JOB_LOGGER.debug(jobId + " Getting username from config " + username);
                    }

                    /**
                     * checks if password is passed as a request parameter
                     */
                    if (receivedBody.containsKey(STATIC.FIELD.PASSWORD)) {
                        password = receivedBody.getString(STATIC.FIELD.PASSWORD);
                        if (password != null && !password.isEmpty()) {
                            /**
                             * checks if password is encrypted, if yes password
                             * is decrypted
                             */
                            if (password.startsWith("encc#")) {
                                FlintLogger.JOB_LOGGER.info("Password is encrypted.");
                                password = com.infiverve.flint.UTIL.decrypt("connector", password); // password decryption
                            }
                        }
                        FlintLogger.JOB_LOGGER.debug(jobId + " Getting password from request ");
                    } else {
                        /**
                         * if password is not passed as a request parameter
                         * password from config parameter will be used
                         */
                        password = this.password;
                        FlintLogger.JOB_LOGGER.debug(jobId + " Getting password from config ");
                    }
                } else {
                    FlintLogger.JOB_LOGGER.error(jobId + STATIC.MESSAGE.WRONG_ACTION);
                    request.sendResponse(-1, STATIC.MESSAGE.WRONG_ACTION, null);
                }

                FlintLogger.JOB_LOGGER.debug(jobId + " Checking for necessary fields username and password");
                /**
                 * username and password validations
                 */
                if (username != null && password != null) {

                    // perform action
                    JsonObject result = TestHelper.authenticate(username, password);

                    //set the result of action performed
                    response.put(STATIC.FIELD.RESULT, result);

                    /**
                     * send response back with result 0 = exit-code, 0 for
                     * success STATIC.MESSAGE.SUCCESS = message response =
                     * result
                     */
                    request.sendResponse(0, STATIC.MESSAGE.SUCCESS, response);

                } else {
                    FlintLogger.JOB_LOGGER.error(jobId + STATIC.MESSAGE.USERNAME_OR_PASSWORD_NULL);
                    /**
                     * send response with error -1 = exit-code, negative
                     * integers for failure
                     * STATIC.MESSAGE.USERNAME_OR_PASSWORD_NULL = failure cause
                     * null = no result due to failure
                     */
                    request.sendResponse(-1, STATIC.MESSAGE.USERNAME_OR_PASSWORD_NULL, null);
                }
            }

        } catch (EncryptionException ex) {
            FlintLogger.JOB_LOGGER.error(jobId + " Exception occured during sending mail using smtp protocol due to " + ex.getLocalizedMessage(), ex);
            request.sendResponse(-1, ex.getLocalizedMessage());
        } catch (Exception e) {
            FlintLogger.JOB_LOGGER.error(jobId + " Exception occured during sending mail using smtp protocol due to " + e.getLocalizedMessage(), e);
            request.sendResponse(-1, e.getLocalizedMessage());
        }

    }

}

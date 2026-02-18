package com.jefflife.mudmk2.gameplay.application.service.required;

import com.jefflife.mudmk2.gameplay.application.service.model.template.StatusVariables;

/**
 * Port for sending status messages to users.
 */
public interface SendStatusMessagePort {
    /**
     * Sends a status message to a user.
     *
     * @param statusVariables the variables for the status message template
     */
    void sendMessage(StatusVariables statusVariables);
}
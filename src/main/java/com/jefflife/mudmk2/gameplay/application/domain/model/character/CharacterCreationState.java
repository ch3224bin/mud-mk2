package com.jefflife.mudmk2.gameplay.application.domain.model.character;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.CharacterClass;
import lombok.Getter;

/**
 * Tracks the state of character creation for a user.
 */
@Getter
public class CharacterCreationState {
    private final Long userId;
    private CreationStep currentStep;
    private String characterName;
    private CharacterClass characterClass;

    public CharacterCreationState(Long userId) {
        this.userId = userId;
        this.currentStep = CreationStep.INITIAL;
    }

    /**
     * Move to the next step in character creation.
     */
    public void nextStep() {
        switch (currentStep) {
            case INITIAL:
                currentStep = CreationStep.AWAITING_NAME;
                break;
            case AWAITING_NAME:
                currentStep = CreationStep.AWAITING_CLASS;
                break;
            case AWAITING_CLASS:
                currentStep = CreationStep.COMPLETE;
                break;
            case COMPLETE:
                // Already complete, do nothing
                break;
        }
    }

    /**
     * Set the character name and move to the next step.
     * @param name the character name
     */
    public void setCharacterName(String name) {
        if (currentStep == CreationStep.AWAITING_NAME) {
            this.characterName = name;
            nextStep();
        }
    }

    /**
     * Set the character class and move to the next step.
     * @param characterClass the character class
     */
    public void setCharacterClass(CharacterClass characterClass) {
        if (currentStep == CreationStep.AWAITING_CLASS) {
            this.characterClass = characterClass;
            nextStep();
        }
    }

    /**
     * Check if character creation is complete.
     * @return true if complete, false otherwise
     */
    public boolean isComplete() {
        return currentStep == CreationStep.COMPLETE;
    }

    /**
     * Check if the state is ready to create a character.
     * @return true if ready, false otherwise
     */
    public boolean isReadyToCreate() {
        return characterName != null && characterClass != null;
    }

    /**
     * Steps in the character creation process.
     */
    public enum CreationStep {
        INITIAL,
        AWAITING_NAME,
        AWAITING_CLASS,
        COMPLETE
    }
}
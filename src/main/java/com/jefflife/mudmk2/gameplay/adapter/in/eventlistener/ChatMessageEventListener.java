package com.jefflife.mudmk2.gameplay.adapter.in.eventlistener;

import com.jefflife.mudmk2.chat.event.ChatMessageEvent;
import com.jefflife.mudmk2.gameplay.application.domain.model.command.*;
import com.jefflife.mudmk2.gameplay.application.port.in.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatMessageEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageEventListener.class);

    // The UseCases are autowired with required=false since we don't have implementations yet
    @Autowired(required = false)
    private SpeakUseCase speakUseCase;

    @Autowired(required = false)
    private AttackUseCase attackUseCase;

    @Autowired(required = false)
    private BuyUseCase buyUseCase;

    @Autowired(required = false)
    private CombineUseCase combineUseCase;

    @Autowired(required = false)
    private MoveUseCase moveUseCase;

    @Autowired(required = false)
    private LookUseCase lookUseCase;

    @Autowired(required = false)
    private StatusUseCase statusUseCase;

    @Autowired(required = false)
    private TakeUseCase takeUseCase;

    @Autowired(required = false)
    private OpenUseCase openUseCase;

    @Autowired(required = false)
    private LockUseCase lockUseCase;

    public ChatMessageEventListener() {
        // Default constructor
    }

    @Async("taskExecutor")
    @EventListener
    public void handleChatMessage(ChatMessageEvent event) {
        // Log the received event
        logger.info("Received chat message event in gameplay module: sender={}, content={}, type={}",
                event.sender(), event.content(), event.type());

        // Parse the message and create the appropriate command
        String content = event.content();
        String sender = event.sender();

        try {
            // Check for status command
            if (content.equals("상태창")) {
                StatusCommand command = new StatusCommand(sender);
                if (statusUseCase != null) {
                    statusUseCase.showStatus(command);
                } else {
                    logger.info("Created StatusCommand: {}", command);
                }
                return;
            }

            // Check for speak command
            if (content.endsWith(" 말")) {
                String message = content.substring(0, content.length() - 2);
                SpeakCommand command = new SpeakCommand(sender, message);
                if (speakUseCase != null) {
                    speakUseCase.speak(command);
                } else {
                    logger.info("Created SpeakCommand: {}", command);
                }
                return;
            }

            // Check for attack command
            Pattern attackPattern = Pattern.compile("(\\S+) 때려");
            Matcher attackMatcher = attackPattern.matcher(content);
            if (attackMatcher.matches()) {
                String target = attackMatcher.group(1);
                AttackCommand command = new AttackCommand(sender, target);
                if (attackUseCase != null) {
                    attackUseCase.attack(command);
                } else {
                    logger.info("Created AttackCommand: {}", command);
                }
                return;
            }

            // Check for buy command
            Pattern buyPattern = Pattern.compile("(\\S+) (\\S+) 사");
            Matcher buyMatcher = buyPattern.matcher(content);
            if (buyMatcher.matches()) {
                String merchant = buyMatcher.group(1);
                String item = buyMatcher.group(2);
                BuyCommand command = new BuyCommand(sender, merchant, item);
                if (buyUseCase != null) {
                    buyUseCase.buy(command);
                } else {
                    logger.info("Created BuyCommand: {}", command);
                }
                return;
            }

            // Check for combine command
            Pattern combinePattern = Pattern.compile("(.+) 조합");
            Matcher combineMatcher = combinePattern.matcher(content);
            if (combineMatcher.matches()) {
                String itemsStr = combineMatcher.group(1);
                List<String> items = Arrays.asList(itemsStr.split(" "));
                CombineCommand command = new CombineCommand(sender, items);
                if (combineUseCase != null) {
                    combineUseCase.combine(command);
                } else {
                    logger.info("Created CombineCommand: {}", command);
                }
                return;
            }

            // Check for look command
            Pattern lookPattern = Pattern.compile("(\\S+) 봐");
            Matcher lookMatcher = lookPattern.matcher(content);
            if (lookMatcher.matches()) {
                String target = lookMatcher.group(1);
                LookCommand command = new LookCommand(sender, target);
                if (lookUseCase != null) {
                    lookUseCase.look(command);
                } else {
                    logger.info("Created LookCommand: {}", command);
                }
                return;
            }

            // Check for take command
            Pattern takePattern = Pattern.compile("(\\S+) (\\S+) 꺼내");
            Matcher takeMatcher = takePattern.matcher(content);
            if (takeMatcher.matches()) {
                String container = takeMatcher.group(1);
                String item = takeMatcher.group(2);
                TakeCommand command = new TakeCommand(sender, container, item);
                if (takeUseCase != null) {
                    takeUseCase.take(command);
                } else {
                    logger.info("Created TakeCommand: {}", command);
                }
                return;
            }

            // Check for open command
            Pattern openPattern = Pattern.compile("(\\S+) 열어");
            Matcher openMatcher = openPattern.matcher(content);
            if (openMatcher.matches()) {
                String target = openMatcher.group(1);
                OpenCommand command = new OpenCommand(sender, target);
                if (openUseCase != null) {
                    openUseCase.open(command);
                } else {
                    logger.info("Created OpenCommand: {}", command);
                }
                return;
            }

            // Check for lock command
            Pattern lockPattern = Pattern.compile("(\\S+) 잠궈");
            Matcher lockMatcher = lockPattern.matcher(content);
            if (lockMatcher.matches()) {
                String target = lockMatcher.group(1);
                LockCommand command = new LockCommand(sender, target);
                if (lockUseCase != null) {
                    lockUseCase.lock(command);
                } else {
                    logger.info("Created LockCommand: {}", command);
                }
                return;
            }

            // Check for move command (simple direction)
            Pattern movePattern = Pattern.compile("(동|서|남|북|위|아래)");
            Matcher moveMatcher = movePattern.matcher(content);
            if (moveMatcher.matches()) {
                String direction = moveMatcher.group(1);
                MoveCommand command = new MoveCommand(sender, direction);
                if (moveUseCase != null) {
                    moveUseCase.move(command);
                } else {
                    logger.info("Created MoveCommand: {}", command);
                }
                return;
            }

            // If no command matched, log a warning
            logger.warn("No command matched for content: {}", content);

        } catch (Exception e) {
            logger.error("Error processing command: {}", e.getMessage(), e);
        }
    }
}

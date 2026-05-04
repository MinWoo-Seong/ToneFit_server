package com.example.ToneFit.correction.ai;

import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Range;
import com.example.ToneFit.session.model.Receiver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "stub", matchIfMissing = true)
public class StubAiCorrectionClient implements AiCorrectionClient {

    @Override
    public AiCorrectionResult correct(String promptContent, Receiver receiver, Purpose purpose,
                                      String original, List<Range> protectedRanges) {
        return new AiCorrectionResult(original, List.of());
    }

    @Override
    public AiFinalizeResult finalizePolish(String promptContent, Receiver receiver, Purpose purpose,
                                           String mergedText, List<Range> protectedRanges) {
        return new AiFinalizeResult(mergedText, "제목");
    }
}

package com.example.ToneFit.correction.ai;

import com.example.ToneFit.session.model.Purpose;
import com.example.ToneFit.session.model.Range;
import com.example.ToneFit.session.model.Receiver;

import java.util.List;

public interface AiCorrectionClient {

    AiCorrectionResult correct(String promptContent,
                               Receiver receiver,
                               Purpose purpose,
                               String original,
                               List<Range> protectedRanges);

    AiFinalizeResult finalizePolish(String promptContent,
                                    Receiver receiver,
                                    Purpose purpose,
                                    String mergedText,
                                    List<Range> protectedRanges);
}

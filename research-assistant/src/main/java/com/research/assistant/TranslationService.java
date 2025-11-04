package com.research.assistant;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    private final Translate translate;

    public TranslationService() {
        translate = TranslateOptions.getDefaultInstance().getService();
    }

    public String detectLanguage(String text) {
        return translate.detect(text).getLanguage();
    }

    public String translateToEnglish(String text) {
        Translation translation = translate.translate(
                text,
                Translate.TranslateOption.targetLanguage("en")
        );
        return translation.getTranslatedText();
    }

    public String translateToOriginal(String text, String targetLang) {
        Translation translation = translate.translate(
                text,
                Translate.TranslateOption.targetLanguage(targetLang)
        );
        return translation.getTranslatedText();
    }
}


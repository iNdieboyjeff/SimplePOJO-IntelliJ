import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.SourceType;

import java.io.File;

/**
 * Created by jeff on 26/07/15.
 */
class SourceGenerationConfig extends DefaultGenerationConfig {

    public boolean usePrimitives = true;
    public boolean useParcel = true;
    public boolean useBuilder = false;
    public boolean useToString = false;
    public boolean useCommons = false;
    public boolean useJoda = false;
    public boolean useDynamic = false;

    @Override
    public boolean isIncludeDynamicAccessors() {
        return useDynamic;
    }

    @Override
    public boolean isIncludeJsr303Annotations() {
        return true;
    }

    @Override
    public boolean isIncludeAdditionalProperties() {
        return false;
    }

    @Override
    public File getTargetDirectory() {
        return super.getTargetDirectory();
    }

    @Override
    public AnnotationStyle getAnnotationStyle() {
        return AnnotationStyle.GSON;
    }

    /**
     * @return {@link SourceType#JSONSCHEMA}
     */
    @Override
    public SourceType getSourceType() {
        return SourceType.JSON;
    }

    @Override
    public boolean isUsePrimitives() {
        return usePrimitives;
    }

    @Override
    public boolean isUseCommonsLang3() {
        return useCommons;
    }

    @Override
    public boolean isUseJodaDates() {
        return useJoda;
    }

    @Override
    public boolean isUseJodaLocalDates() {
        return useJoda;
    }

    @Override
    public boolean isUseJodaLocalTimes() {
        return useJoda;
    }

    @Override
    public boolean isIncludeHashcodeAndEquals() {
        return false;
    }

    @Override
    public boolean isIncludeToString() {
        return useToString;
    }

    @Override
    public boolean isParcelable() {
        return useParcel;
    }

    @Override
    public boolean isInitializeCollections() {
        return true;
    }

    @Override
    public boolean isGenerateBuilders() {
        return useBuilder;
    }
}

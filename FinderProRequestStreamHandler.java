import Handlers.*;
import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;

public class FinderProRequestStreamHandler extends SkillStreamHandler
{
    private static Skill getSkill()
    {
        return Skills.standard()
                .addRequestHandlers(
                        new LaunchIntentRequestHandler(),
                        new GetBucketNameIntentRequestHandler(),
                        new ThisIsMyBucketNameIntentRequestHandler(),
                        new ThisIsNotMyBucketNameIntentRequestHandler(),
                        new GetFileNameIntentRequestHandler(),
                        new ThisIsMyFileNameIntentRequestHandler(),
                        new ThisIsNotMyFileNameIntentRequestHandler(),
                        new GetFileFormatIntentRequestHandler(),
                        new ThisIsMyFileFormatIntentRequestHandler(),
                        new ThisIsNotMyFileFormatIntentRequestHandler(),
                        new ThisIsMyFileUrlIntentRequestHandler(),
                        new ThisIsNotMyFileUrlIntentRequestHandler(),
                        new SearchTextIntentRequestHandler(),
                        new SearchObjectIntentRequestHandler(),
                        new GetTargetBucketNameIntentRequestHandler(),
                        new ThisIsMyTargetBucketNameIntentRequestHandler(),
                        new ThisIsNotMyTargetBucketNameIntentRequestHandler(),
                        new GetTargetFileNameIntentRequestHandler(),
                        new ThisIsMyTargetFileNameIntentRequestHandler(),
                        new ThisIsNotMyTargetFileNameIntentRequestHandler(),
                        new GetTargetFileFormatIntentRequestHandler(),
                        new ThisIsMyTargetFileFormatIntentRequestHandler(),
                        new ThisIsNotMyTargetFileFormatIntentRequestHandler(),
                        new ThisIsMyTargetFileUrlIntentRequestHandler(),
                        new ThisIsNotMyTargetFileUrlIntentRequestHandler(),
                        new ShowTheTaskMenuIntentRequestHandler(),
                        new TaskNameIntentRequestHandler(),
                        new StorageHelperIntentRequestHandler(),
                        new FileHelperIntentRequestHandler(),
                        new HelpIntentRequestHandler(),
                        new StopOrCancelIntentRequestHandler(),
                        new YesIntentRequestHandler(),
                        new NoIntentRequestHandler(),
                        new FallbackIntentRequestHandler()
                )
                .addExceptionHandlers(new ExceptionRequestHandler())
                .withSkillId("amzn1.ask.skill.fa49a430-8aab-4080-aaac-8179aac70d40")
                .build();
    }

    public FinderProRequestStreamHandler() {
        super(getSkill());
    }
}

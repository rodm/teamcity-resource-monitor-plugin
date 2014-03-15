package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TeamCity8BuildTypeAdapterTest {

    private TeamCity8BuildType buildType;
    private BuildTypeAdapter adapter;

    @Before
    public void setup() {
        buildType = mock(TeamCity8BuildType.class);
        adapter = new TeamCity8BuildTypeAdapter(buildType);
    }

    @Test
    public void shouldDelegateGettingBuildTypeId() {
        adapter.getBuildTypeId();
        verify(buildType).getBuildTypeId();
    }

    @Test
    public void shouldDelegateGettingFullName() {
        adapter.getFullName();
        verify(buildType).getFullName();
    }

    @Test
    public void shouldDelegateGettingExternalIdByReturningBuildTypeId() {
        adapter.getExternalId();
        verify(buildType).getExternalId();
    }

    interface TeamCity8BuildType extends SBuildType {
        @NotNull
        String getExternalId();
    }
}

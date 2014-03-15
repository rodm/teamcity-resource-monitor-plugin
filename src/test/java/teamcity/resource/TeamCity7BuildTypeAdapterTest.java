package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TeamCity7BuildTypeAdapterTest {

    private SBuildType buildType;
    private BuildTypeAdapter adapter;

    @Before
    public void setup() {
        buildType = mock(SBuildType.class);
        adapter = new TeamCity7BuildTypeAdapter(buildType);
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
        verify(buildType).getBuildTypeId();
    }
}

package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectManagerAdapterTest {

    private static final String BUILD_TYPE_ID = "bt123";

    private static final byte TEAMCITY_VERSION_7 = 7;
    private static final byte TEAMCITY_VERSION_8 = 8;

    private SBuildServer server;
    private ProjectManager projectManager;
    private ProjectManagerAdapter projectManagerAdapter;

    @Before
    public void setup() {
        server = mock(SBuildServer.class);
        projectManager = mock(ProjectManager.class);
        projectManagerAdapter = new ProjectManagerAdapter(server, projectManager);
    }

    @Test
    public void shouldDelegateFindingBuildTypeById() {
        projectManagerAdapter.findBuildTypeById(BUILD_TYPE_ID);
        verify(projectManager).findBuildTypeById(BUILD_TYPE_ID);
    }

    @Test
    public void shouldDelegateGettingBuildTypes() {
        projectManagerAdapter.getAllBuildTypes();
        verify(projectManager).getAllBuildTypes();
    }

    @Test
    public void shouldReturnTeamCity7BuildTypeAdapterWhenRunningOnTeamCity7() {
        SBuildType buildType = mock(SBuildType.class);
        when(server.getServerMajorVersion()).thenReturn(TEAMCITY_VERSION_7);
        when(projectManager.findBuildTypeById(BUILD_TYPE_ID)).thenReturn(buildType);

        BuildType buildTypeAdapter = projectManagerAdapter.findBuildTypeById(BUILD_TYPE_ID);
        assertThat(buildTypeAdapter, isInstanceOf(TeamCity7BuildTypeAdapter.class));
    }

    @Test
    public void shouldReturnTeamCity8BuildTypeAdapterWhenRunningOnTeamCity8() {
        SBuildType buildType = mock(SBuildType.class);
        when(server.getServerMajorVersion()).thenReturn(TEAMCITY_VERSION_8);
        when(projectManager.findBuildTypeById(BUILD_TYPE_ID)).thenReturn(buildType);

        BuildType buildTypeAdapter = projectManagerAdapter.findBuildTypeById(BUILD_TYPE_ID);
        assertThat(buildTypeAdapter, isInstanceOf(TeamCity8BuildTypeAdapter.class));
    }

    private Matcher isInstanceOf(Class clazz) {
        return new IsInstanceOf(clazz);
    }
}

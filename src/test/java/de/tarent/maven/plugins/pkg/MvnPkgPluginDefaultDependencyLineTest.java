package de.tarent.maven.plugins.pkg;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MvnPkgPluginDefaultDependencyLineTest extends AbstractMvnPkgPluginTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void OverrideDefaultDependencyLine() throws Exception, MojoExecutionException {
        packagingPlugin = mockPackagingEnvironment(DEFAULTDEPENDENCYLINE, "override");
        packagingPlugin.execute();
        Assert.assertEquals("openjdk-7-jre-headless", packagingPlugin.targetConfigurations.get(0)
                .getDefaultDependencyLine());
    }

    @Test
    public void NotOverrideDefaultDependencyLine() throws Exception, MojoExecutionException {
        packagingPlugin = mockPackagingEnvironment(DEFAULTDEPENDENCYLINE, "not_override");
        packagingPlugin.execute();
        Assert.assertNull(packagingPlugin.targetConfigurations.get(1)
                .getDefaultDependencyLine());
    }

    private Packaging mockPackagingEnvironment(String pomFilename, String target) throws Exception {
        return (Packaging) mockEnvironment(pomFilename, "pkg", true, target);
    }
}

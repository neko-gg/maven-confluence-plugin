package org.bsc.maven.confluence.plugin;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;
import org.bsc.confluence.model.Site;
import org.bsc.confluence.model.SiteFactory;

import biz.source_code.miniTemplator.MiniTemplator;
import biz.source_code.miniTemplator.MiniTemplator.VariableNotDefinedException;

/**
 *
 * @author bsorrentino
 */
public abstract class AbstractBaseConfluenceSiteMojo extends AbstractBaseConfluenceMojo implements SiteFactory.Model {

    /**
     * attachment folder
     */
    @Parameter(defaultValue = "${basedir}/src/site/confluence/attachments")
    private java.io.File attachmentFolder;

    /**
     * children folder
     */
    @Parameter(defaultValue = "${basedir}/src/site/confluence/children")
    private java.io.File childrenFolder;

    /**
     * Confluence Page Title
     * 
     * @since 3.1.3
     */

    @Parameter(alias = "title", property = "confluence.page", defaultValue = "${project.build.finalName}")
    private String pageTitle;

    /**
     * site xml descriptor
     * 
     * @since 3.3.0
     */
    @Parameter(defaultValue = "${basedir}/src/site/confluence/site.xml")
    protected java.io.File siteDescriptor;

    /**
     * 
     * @return
     */
    public File getSiteDescriptor() {
        return siteDescriptor;
    }

    protected boolean isSiteDescriptorValid() {
        return (siteDescriptor != null && siteDescriptor.exists() && siteDescriptor.isFile());
    }

    /**
     *
     */
    public AbstractBaseConfluenceSiteMojo() {
    }

    protected File getChildrenFolder() {
        return childrenFolder;
    }

    protected File getAttachmentFolder() {
        return attachmentFolder;
    }

    /**
     *
     * @return
     */
    protected final String getPageTitle() {
        return pageTitle;
    }

    /**
     *
     * @param title
     */
    public void setPageTitle(String title) {
        this.pageTitle = title;
    }

    public void addStdProperties(MiniTemplator t) {
        java.util.Map<String, String> props = getProperties();

        if (props == null || props.isEmpty()) {
            getLog().info("no properties set!");
        } else {
            for (java.util.Map.Entry<String, String> e : props.entrySet()) {

                try {
                    t.setVariable(e.getKey(), e.getValue(), true /* isOptional */);
                } catch (VariableNotDefinedException e1) {
                    getLog().debug(String.format("variable %s not defined in template", e.getKey()));
                }
            }
        }

    }

    /**
     * 
     * @return
     */
    @Override
    public Site createSiteFromModel() {

        if (!isSiteDescriptorValid()) {
            getLog().warn("siteDescriptor is not valid!");
            return null;
        }

        try {
            return createFrom(siteDescriptor);
        } catch (Exception ex) {
            getLog().error("error creating site from model!", ex);
            return null;
        }
    }

}

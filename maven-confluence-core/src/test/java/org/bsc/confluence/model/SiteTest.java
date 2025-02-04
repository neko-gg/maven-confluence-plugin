package org.bsc.confluence.model;

import static java.lang.String.format;
import static org.bsc.confluence.model.SiteProcessor.processMarkdown;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;

import lombok.val;

/**
 * 
 * @author bsorrentino
 *
 */
public class SiteTest implements SiteFactory.Model {

    @Override
    public Site createSiteFromModel() {
        val path = Paths.get("src", "test", "resources", "site.yaml");
        try {
            return createFrom( path.toFile() );
        } catch (Exception e) {
            throw new RuntimeException(String.format("error reading site descriptor at [%s]", path), e);
        }
    }

    Site site;
    
    @Before
    public void loadSite() {
        site = createSiteFromModel();
        site.setBasedir( Paths.get("src", "test", "resources"));
    }
    
    @Test
    public void validateName() {
        
        final Pattern p = Pattern.compile("^(.*)\\s+$|^\\s+(.*)");
        assertThat( p.matcher("name").matches(), is(false));
        assertThat( p.matcher("name ").matches(), is(true));
        assertThat( p.matcher("name  ").matches(), is(true));
        assertThat( p.matcher(" name").matches(), is(true));
        assertThat( p.matcher("  name").matches(), is(true));
        assertThat( p.matcher(" name ").matches(), is(true));
        assertThat( p.matcher(" name  ").matches(), is(true));
    }
    
    @Test
    public void shouldSupportRefLink() throws IOException {
        val parentPageTitle = "Test";
        val stream = getClass().getClassLoader().getResourceAsStream("withRefLink.md");
        assertThat( stream, IsNull.notNullValue());
        val inputStream = processMarkdown(site, Optional.empty(), stream, parentPageTitle);
        assertThat( inputStream, IsNull.notNullValue());
        val converted = IOUtils.toString(inputStream).split("\n+");
        int i = 2;
        
        
        assertThat(converted[i++], equalTo(format("* This is a relative link to another page [SECOND PAGE|%s - page 2|].", parentPageTitle)) );
        assertThat(converted[i++], equalTo("* This one is [inline|http://google.com|Google].") );
        assertThat(converted[i++], equalTo("* This one is [inline *wo* title|http://google.com|].") );
        assertThat(converted[i++], containsString("[google|http://google.com]"));
        assertThat(converted[i++], containsString("[more complex google|http://google.com|Other google]"));  
        assertThat(converted[i++], equalTo("* This is my [relative|relativepage|] link defined after.") );
        assertThat(converted[i++], containsString("[rel|Test - relativeagain]"));
    }

    @Test
    public void shouldSupportImgRefLink() throws IOException {
        val parentPageTitle = "Test IMG";
        val stream = getClass().getClassLoader().getResourceAsStream("withImgRefLink.md");
        assertThat( stream, IsNull.notNullValue());
        val inputStream = processMarkdown(site,Optional.empty(), stream, parentPageTitle);
        assertThat( inputStream, IsNull.notNullValue());
        val converted = IOUtils.toString(inputStream).split("\n+");

        int i = 2;
        assertThat(converted[i++], containsString("!http://www.lewe.com/wp-content/uploads/2016/03/conf-icon-64.png|conf-icon!"));
        assertThat(converted[i++], containsString("!${page.title}^conf-icon-64.png|conf-icon!"));
        assertThat(converted[i++], containsString("!${page.title}^conf-icon-64.png|conf-icon!"));
        assertThat(converted[i++], containsString("!http://www.lewe.com/wp-content/uploads/2016/03/conf-icon-64.png|conf-icon-y!"));
        assertThat(converted[i++], containsString("!http://www.lewe.com/wp-content/uploads/2016/03/conf-icon-64.png|conf-icon-y1!"));
        assertThat(converted[i++], containsString("!${page.title}^conf-icon-64.png|conf-icon-y2!"));
        assertThat(converted[i++], containsString("!${page.title}^conf-icon-64.png|conf-icon-none!"));
    }

    @Test
    public void shouldSupportSimpleNode() throws IOException {
        val parentPageTitle = "Test";

        final InputStream stream = getClass().getClassLoader().getResourceAsStream("simpleNodes.md");
        assertThat( stream, IsNull.notNullValue());
        final InputStream inputStream = processMarkdown(site,Optional.empty(), stream, parentPageTitle);
        assertThat( inputStream, IsNull.notNullValue());
        final String converted = IOUtils.toString(inputStream);

        assertThat("All forms of HRules should be supported", converted, containsString("----\n1\n----\n2\n----\n3\n----\n4\n----"));
        /* only when Extensions.SMARTS is activated
        assertThat(converted, containsString("&hellip;"));
        assertThat(converted, containsString("&ndash;"));
        assertThat(converted, containsString("&mdash;"));
        */
        assertThat(converted, containsString("Forcing a line-break\nNext line in the list"));
        assertThat(converted, containsString("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
    }
    
    @Test
    public void shouldCreateSpecificNoticeBlock() throws IOException {
        val parentPageTitle = "Test Macro";

        final InputStream stream = getClass().getClassLoader().getResourceAsStream("createSpecificNoticeBlock.md");
        assertThat( stream, IsNull.notNullValue());
        final InputStream inputStream = processMarkdown(site, Optional.empty(), stream, parentPageTitle);
        assertThat( inputStream, IsNull.notNullValue());
        final String converted = IOUtils.toString(inputStream);

        assertThat(converted, containsString("{info:title=About me}\n"));
        assertThat("Should not generate unneeded param 'title'", converted, not(containsString("{note:title=}\n")));
        assertThat(converted, containsString("{tip:title=About you}\n"));
        assertThat(converted, containsString("bq. test a simple blockquote"));
        assertThat(converted, containsString("{quote}\n"));
        assertThat(converted, containsString("* one\n* two"));
        assertThat(converted, containsString("a *strong* and _pure_ feeling"));

    }
    
    
}
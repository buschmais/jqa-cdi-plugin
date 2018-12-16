package com.buschmais.jqassistant.plugin.cdi.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.plugin.cdi.api.model.BeansXmlDescriptor;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.alternative.AlternativeBean;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.alternative.AlternativeStereotype;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.decorator.DecoratorBean;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.interceptor.CustomInterceptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the CDI concepts.
 */
public class BeansXmlScannerPluginIT extends AbstractJavaPluginIT {

    /**
     * Verifies scanning of the beans descriptor.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void beansDescriptor() throws IOException, NoSuchMethodException, NoSuchFieldException {
        scanClassPathDirectory(getClassesDirectory(BeansXmlScannerPluginIT.class));
        store.beginTransaction();
        List<Object> column = query("MATCH (beans:Cdi:Beans:Xml:File) RETURN beans").getColumn("beans");
        assertThat(column.size(), equalTo(1));
        BeansXmlDescriptor beansXmlDescriptor = (BeansXmlDescriptor) column.get(0);
        assertThat(beansXmlDescriptor.getFileName(), equalTo("/META-INF/beans.xml"));
        assertThat(beansXmlDescriptor.getVersion(), equalTo("1.1"));
        assertThat(beansXmlDescriptor.getBeanDiscoveryMode(), equalTo("annotated"));
        assertThat(beansXmlDescriptor.getAlternatives(),
                allOf(hasItem(typeDescriptor(AlternativeBean.class)), hasItem(typeDescriptor(AlternativeStereotype.class))));
        assertThat(beansXmlDescriptor.getDecorators(), hasItem(typeDescriptor(DecoratorBean.class)));
        assertThat(beansXmlDescriptor.getInterceptors(), hasItem(typeDescriptor(CustomInterceptor.class)));
        store.commitTransaction();
    }

    @Test
    public void invalidBeansDescriptor() throws IOException, NoSuchMethodException, NoSuchFieldException {
        scanClassPathDirectory(new File(getClassesDirectory(BeansXmlScannerPluginIT.class), "invalid"));
        store.beginTransaction();
        List<Object> column = query("MATCH (beans:Cdi:Beans:Xml:File) RETURN beans").getColumn("beans");
        assertThat(column.size(), equalTo(1));
        BeansXmlDescriptor beansXmlDescriptor = (BeansXmlDescriptor) column.get(0);
        assertThat(beansXmlDescriptor.getFileName(), equalTo("/META-INF/beans.xml"));
        assertThat(beansXmlDescriptor.isXmlWellFormed(), equalTo(false));
        store.commitTransaction();
    }
}

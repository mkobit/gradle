/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.plugin.devel.plugins;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.XmlProvider;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.ivy.IvyModuleDescriptorSpec;
import org.gradle.api.publish.ivy.IvyPublication;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.plugin.devel.PluginArtifactCoordinates;
import org.gradle.plugin.devel.PluginDeclaration;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class IvyPluginPublishingRules extends RuleSource {

    @Mutate
    public void addPluginPublications(PublishingExtension publishing, GradlePluginDevelopmentExtension pluginDevelopment, ServiceRegistry services) {
        if (!pluginDevelopment.isAutomatedPublishing()) {
            return;
        }
        SoftwareComponentContainer componentContainer = services.get(SoftwareComponentContainer.class);
        SoftwareComponent component = componentContainer.getByName("java");

        PublicationContainer publications = publishing.getPublications();
        PluginArtifactCoordinates coordinates = pluginDevelopment.getPluginArtifactCoordinates();
        NamedDomainObjectContainer<PluginDeclaration> declaredPlugins = pluginDevelopment.getPlugins();

        createIvyPluginPublication(component, coordinates, publications);
        for (PluginDeclaration declaration : declaredPlugins) {
            createIvyMarkerPublication(declaration, coordinates, publications);
        }
    }

    private void createIvyPluginPublication(SoftwareComponent component, PluginArtifactCoordinates coordinates, PublicationContainer publications) {
        IvyPublication publication = publications.create("pluginIvy", IvyPublication.class);
        publication.from(component);
        publication.setOrganisation(coordinates.getGroup());
        publication.setModule(coordinates.getName());
        publication.setRevision(coordinates.getVersion());
    }

    private void createIvyMarkerPublication(PluginDeclaration declaration, final PluginArtifactCoordinates coordinates, PublicationContainer publications) {
        String pluginId = declaration.getId();
        IvyPublication publication = publications.create(declaration.getName() + "PluginMarkerIvy", IvyPublication.class);
        publication.setOrganisation(pluginId);
        publication.setModule(pluginId);
        publication.descriptor(new Action<IvyModuleDescriptorSpec>() {
            @Override
            public void execute(IvyModuleDescriptorSpec descriptor) {
                descriptor.withXml(new Action<XmlProvider>() {
                    @Override
                    public void execute(XmlProvider xmlProvider) {
                        Element root = xmlProvider.asElement();
                        Document document = root.getOwnerDocument();
                        Node dependencies = root.getElementsByTagName("dependencies").item(0);
                        Node dependency = dependencies.appendChild(document.createElement("dependency"));
                        Attr org = document.createAttribute("org");
                        org.setValue(coordinates.getGroup());
                        dependency.getAttributes().setNamedItem(org);
                        Attr name = document.createAttribute("name");
                        name.setValue(coordinates.getName());
                        dependency.getAttributes().setNamedItem(name);
                        Attr rev = document.createAttribute("rev");
                        rev.setValue(coordinates.getVersion());
                        dependency.getAttributes().setNamedItem(rev);
                    }
                });
            }
        });
    }
}

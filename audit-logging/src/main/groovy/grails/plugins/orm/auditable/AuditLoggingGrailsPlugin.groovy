/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package grails.plugins.orm.auditable

import grails.plugins.Plugin
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.core.Datastore

/**
 * @author Robert Oschwald
 * @author Aaron Long
 * @author Shawn Hartsock
 * @author Graeme Rocher
 *
 * Credit is due to the following other projects,
 * first is Kevin Burke's HibernateEventsGrailsPlugin
 * second is the AuditLogging post by Rob Monie at
 * http://www.hibernate.org/318.html
 *
 * Combined the two sources to create a Grails
 * Audit Logging plugin that will track individual
 * changes to columns.
 *
 * See Documentation:
 * http://grails.org/plugin/audit-logging
 *
 */
@Slf4j
@SuppressWarnings("GroovyUnusedDeclaration")
class AuditLoggingGrailsPlugin extends Plugin {
    def grailsVersion = '3.0.0 > *'

    def title = "Audit Logging Plugin"
    def authorEmail = "roos@symentis.com"
    def description = """ 
        Automatically log change events for domain objects.
        The Audit Logging plugin additionally adds an instance hook to domain objects that allows you to hang Audit events off of them.
        The events include onSave, onChange, and onDelete.
        When called, the event handlers have access to oldObj and newObj definitions that will allow you to take action on what has changed.
    """
    String documentation = 'http://grails.org/plugin/audit-logging'
    String license = 'APACHE'

    def developers = [
        [name: 'Robert Oschwald', email: 'roos@symentis.com'],
        [name: 'Elmar Kretzer', email: 'elkr@symentis.com'],
        [name: 'Aaron Long', email: 'longwa@gmail.com']
    ]

    def issueManagement = [system: 'GitHub', url: 'https://github.com/robertoschwald/grails-audit-logging-plugin/issues']
    def scm = [url: 'https://github.com/robertoschwald/grails-audit-logging-plugin']
    def loadAfter = ['core', 'dataSource']

    // Register generic GORM listener
    @Override
    void doWithApplicationContext() {
        ConfigObject config = AuditLoggingConfigUtils.auditConfig

        // Allow a collection of ignored data store names
        Set<String> excludedDataStores = (config.getProperty("excludedDataSources") ?: []) as Set<String>

        applicationContext.getBeansOfType(Datastore).each { String key, Datastore datastore ->
            String dataSourceName = datastore.metaClass.getProperty(datastore, 'dataSourceName')

            if (!config.disabled && !excludedDataStores.contains(dataSourceName)) {
                applicationContext.addApplicationListener(new AuditLogListener(datastore, grailsApplication))
            }
            if (config.stampEnabled && !excludedDataStores.contains(dataSourceName)) {
                applicationContext.addApplicationListener(new StampListener(datastore, grailsApplication))
            }
        }
    }

    @Override
    void onConfigChange(Map<String, Object> event) {
        AuditLoggingConfigUtils.resetAuditConfig()
    }
}

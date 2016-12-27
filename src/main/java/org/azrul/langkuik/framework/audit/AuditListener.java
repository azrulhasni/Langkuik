/*
 * Copyright 2014 azrulhasni.
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

package org.azrul.langkuik.framework.audit;

import org.azrul.langkuik.security.role.UserSecurityUtils;
import org.azrul.langkuik.system.model.audit.AuditMetadata;
import org.hibernate.envers.RevisionListener;

/**
 *
 * @author azrulhasni
 */
public class AuditListener implements RevisionListener {

 @Override
 public void newRevision(Object revisionEntity) {
  AuditMetadata auditMetadata=(AuditMetadata) revisionEntity;
  auditMetadata.setUpdater(UserSecurityUtils.getCurrentUser());
 }

}
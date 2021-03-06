/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.flowable.cmmn.engine.impl.cmd;

import java.io.Serializable;
import java.util.List;

import org.flowable.cmmn.engine.impl.util.CommandContextUtil;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

/**
 * @author Tijs Rademakers
 */
public class GetIdentityLinksForTaskCmd implements Command<List<IdentityLink>>, Serializable {

    private static final long serialVersionUID = 1L;
    protected String taskId;

    public GetIdentityLinksForTaskCmd(String taskId) {
        this.taskId = taskId;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<IdentityLink> execute(CommandContext commandContext) {
        TaskEntity task = CommandContextUtil.getTaskService().getTask(taskId);

        List<IdentityLink> identityLinks = (List) task.getIdentityLinks();

        // assignee is not part of identity links in the db.
        // so if there is one, we add it here.
        // @Tom: we discussed this long on skype and you agreed ;-)
        // an assignee *is* an identityLink, and so must it be reflected in the API
        //
        // Note: we cant move this code to the TaskEntity (which would be cleaner),
        // since the task.delete cascaded to all associated identityLinks
        // and of course this leads to exception while trying to delete a non-existing identityLink
        if (task.getAssignee() != null) {
            IdentityLinkEntity identityLink = CommandContextUtil.getIdentityLinkService().createIdentityLink();
            identityLink.setUserId(task.getAssignee());
            identityLink.setType(IdentityLinkType.ASSIGNEE);
            identityLink.setTaskId(task.getId());
            identityLinks.add(identityLink);
        }
        if (task.getOwner() != null) {
            IdentityLinkEntity identityLink = CommandContextUtil.getIdentityLinkService().createIdentityLink();
            identityLink.setUserId(task.getOwner());
            identityLink.setTaskId(task.getId());
            identityLink.setType(IdentityLinkType.OWNER);
            identityLinks.add(identityLink);
        }

        return identityLinks;
    }

}

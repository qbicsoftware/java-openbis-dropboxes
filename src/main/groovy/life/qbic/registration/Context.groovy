package life.qbic.registration

import life.qbic.datamodel.dtos.projectmanagement.ProjectCode
import life.qbic.datamodel.dtos.projectmanagement.ProjectIdentifier
import life.qbic.datamodel.dtos.projectmanagement.ProjectSpace

/**
 * <p>Provides contextual information during registration of incoming data.</p>
 *
 * @since 1.0.0
 */
class Context {

    ProjectCode projectCode

    ProjectSpace projectSpace

    ProjectIdentifier projectIdentifier

}

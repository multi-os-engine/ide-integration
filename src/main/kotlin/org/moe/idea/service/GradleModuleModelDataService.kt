package org.moe.idea.service

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.Key
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.moe.idea.facet.gradle.GradleFacet
import org.moe.idea.model.GradleModuleModel

class GradleModuleModelDataService : ModuleModelDataService<GradleModuleModel>() {
    override fun getTargetDataKey(): Key<GradleModuleModel> = GradleModuleModel.KEY

    override fun importData(
        toImport: MutableCollection<out DataNode<GradleModuleModel>>,
        project: Project,
        modelsProvider: IdeModifiableModelsProvider,
        modelsByModuleName: Map<String, GradleModuleModel>
    ) {
        modelsProvider.modules.forEach { module ->
            val gradleModuleModel = modelsByModuleName[module.name]
            if (gradleModuleModel != null) {
                val facetModel = modelsProvider.getModifiableFacetModel(module)
                var facet = facetModel.getFacetByType(GradleFacet.TYPE_ID)
                if (facet == null) {
                    val facetType = GradleFacet.getFacetType()
                    facet = facetType.createFacet(
                        module,
                        GradleFacet.FACET_NAME,
                        facetType.createDefaultConfiguration(),
                        null
                    )
                    facetModel.addFacet(facet, ExternalSystemApiUtil.toExternalSource(GradleConstants.SYSTEM_ID))
                }

                facet.gradleModuleModel = gradleModuleModel
            }
        }
    }

    override fun removeData(
        toRemoveComputable: Computable<out MutableCollection<out Module>>,
        toIgnore: MutableCollection<out DataNode<GradleModuleModel>>,
        projectData: ProjectData,
        project: Project,
        modelsProvider: IdeModifiableModelsProvider
    ) {
        toRemoveComputable.get().forEach { module ->
            val facetModel = modelsProvider.getModifiableFacetModel(module)
            val facets = facetModel.getFacetsByType(GradleFacet.TYPE_ID)
            facets.forEach(facetModel::removeFacet)
        }
    }
}
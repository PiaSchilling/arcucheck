package core.model.deviation

import core.model.PUMLClass

data class Deviation(
    val level: DeviationLevel,
    val area: DeviationArea,
    val type: DeviationType,
    val affectedClass: PUMLClass,
    val title: String,
    val description: String,
)

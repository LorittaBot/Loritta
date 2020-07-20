package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.FlowContent
import net.perfectdreams.loritta.embededitor.data.AdditionalRenderInfo
import net.perfectdreams.loritta.embededitor.utils.MessageTagSection

typealias MODIFY_TAG_CALLBACK = (FlowContent.(currentElement: CommonAttributeGroupFacade, input: MessageTagSection, additionalInfo: AdditionalRenderInfo?) -> (Unit))
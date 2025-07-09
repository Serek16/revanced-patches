package app.revanced.patches.youtube.layout.searchresults

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/DebloatSearchResultsPatch;"

@Suppress("unused")
val debloatSearchResultsPatch = bytecodePatch(
    name = "Debloat search results",
    description = """Silently adds " before:2099" to user search query resulting in removing
                     "People also watched this video", "You might also like this", "Previously watched",
                     "From related searches", "New for you", "Channels new to you" from the search results"""
) {
    compatibleWith("com.google.android.youtube"("20.12.46"))
    execute {
        // Append " before:2099" to the search query
        // TODO: Right now it skips the patch when voice search is used. To make it work appended
        //  part must be trimmed before the voice assistant repeats what user tries to search
        searchQueryFingerprint.method.let {
            val searchQueryIndex = it.implementation!!.instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.CONST_STRING &&
                        instruction.getReference<StringReference>()?.string == "search_query"
            }
            it.apply {
                val instruction = getInstruction<FiveRegisterInstruction>(searchQueryIndex + 1)
                val register1 = instruction.registerC
                val register2 = instruction.registerD
                addInstructions(
                    searchQueryIndex + 3,
                    """
                        invoke-static {v$register2, v$register1}, $EXTENSION_CLASS_DESCRIPTOR->appendSearchQuerySkipVoiceSearch(Ljava/lang/String;Landroid/os/Bundle;)Ljava/lang/String;
                        move-result-object v$register2
                    """
                )
            }
        }

        // Hide appended part in a search bar
        // Inject trimSearchQuery right before setText
        searchBarFingerprint.method.let {
            val searchBarIndex = it.implementation!!.instructions.indexOfLast { instruction ->
                instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                        instruction.getReference<MethodReference>()?.name?.equals("setText") == true
            }
            it.apply {
                val register = getInstruction<FiveRegisterInstruction>(searchBarIndex).registerD
                addInstructions(
                    searchBarIndex,
                    """
                        invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->trimSearchQuery(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                        move-result-object v$register
                    """
                )
            }
        }

        // Hide appended part when the user makes a typo and app will give a hint with correct query
        // "Did you mean: <corrected_query>"
        // Inject trimSearchQuery right before creating SpannableString which is used in setText right after
        val didYouMeanClass = didYouMeanConstructorFingerprint.originalClassDef
        didYouMeanFingerprint.match(didYouMeanClass).method.let {
            val didYouMeanIndex = it.implementation!!.instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                        instruction.getReference<MethodReference>()?.let { methodRef ->
                            methodRef.parameterTypes.size == 4 &&
                                    methodRef.parameterTypes[0] == "Ljava/lang/CharSequence;" &&
                                    methodRef.parameterTypes[1] == "Ljava/lang/CharSequence;" &&
                                    methodRef.parameterTypes[3] == "Ljava/lang/String;" &&
                                    methodRef.returnType == "Ljava/lang/CharSequence;"
                        } == true
            }
            it.apply {
                val register = getInstruction<FiveRegisterInstruction>(didYouMeanIndex).registerE
                addInstructions(
                    didYouMeanIndex,
                    """
                        invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->trimSearchQuery(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                        move-result-object v$register
                    """
                )
            }
        }

        // Hide appended part when the user makes a typo, but the app decides to correct query itself and show two hints
        // Inject trimSearchQuery right before creating SpannableString which is used in setText right after
        val showingAndSearchClass = showingAndSearchConstructorFingerprint.originalClassDef
        showingAndSearchFingerprint.match(showingAndSearchClass).method.let {
            // First hint: "Showing results for <corrected_query>"
            val showingIndex = it.implementation!!.instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                        instruction.getReference<MethodReference>()?.let { methodRef ->
                            methodRef.parameterTypes.size == 4 &&
                                    methodRef.parameterTypes[0] == "Ljava/lang/CharSequence;" &&
                                    methodRef.parameterTypes[1] == "Ljava/lang/CharSequence;" &&
                                    methodRef.parameterTypes[3] == "Ljava/lang/String;" &&
                                    methodRef.returnType == "Ljava/lang/CharSequence;"
                        } == true
            }
            it.apply {
                val register = getInstruction<FiveRegisterInstruction>(showingIndex).registerE
                addInstructions(
                    showingIndex,
                    """
                        invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->trimSearchQuery(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                        move-result-object v$register
                    """
                )
            }
            // Second hint: "Search instead for <query>"
            val searchIndex = it.implementation!!.instructions.indexOfLast { instruction ->
                instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                        instruction.getReference<MethodReference>()?.let { methodRef ->
                            methodRef.parameterTypes.size == 4 &&
                                    methodRef.parameterTypes[0] == "Ljava/lang/CharSequence;" &&
                                    methodRef.parameterTypes[1] == "Ljava/lang/CharSequence;" &&
                                    methodRef.parameterTypes[3] == "Ljava/lang/String;" &&
                                    methodRef.returnType == "Ljava/lang/CharSequence;"
                        } == true
            }
            it.apply {
                val register = getInstruction<FiveRegisterInstruction>(searchIndex).registerE
                addInstructions(
                    searchIndex,
                    """
                       invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->trimSearchQuery(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                       move-result-object v$register
                    """
                )
            }
        }

        // Hide appended part when the user clicks on a search bar
        // Inject trimSearchQuery right before setText
        onClickSearchBarFingerprint.method.let {
            val onClickSearchBarIndex = it.implementation!!.instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                        instruction.getReference<MethodReference>()?.name?.equals("setText") == true
            }
            it.apply {
                val register = getInstruction<FiveRegisterInstruction>(onClickSearchBarIndex).registerD
                addInstructions(
                    onClickSearchBarIndex,
                    """
                        invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->trimSearchQuery(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                        move-result-object v$register
                    """
                )
            }
        }
    }
}

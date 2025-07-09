package app.revanced.patches.youtube.layout.searchresults

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction

internal val searchQueryFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;")
    returns("Landroid/view/View;")
    opcodes(
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.INVOKE_DIRECT,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE
    )
}

internal val searchBarFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;")
    returns("Landroid/view/View;")
    opcodes(
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.INVOKE_DIRECT,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE
    )
}

internal val didYouMeanConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { constructor, _ ->
        constructor.implementation?.instructions?.any { instruction ->
            instruction.opcode == Opcode.CONST &&
                    instruction is NarrowLiteralInstruction &&
                    instruction.narrowLiteral == 0x7f0e01d8
        } == true
    }
}

internal val didYouMeanFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.BRIDGE, AccessFlags.SYNTHETIC)
    returns("V")
}

internal val showingAndSearchConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { constructor, _ ->
        constructor.implementation?.instructions?.any { instruction ->
            instruction.opcode == Opcode.CONST &&
                    instruction is NarrowLiteralInstruction &&
                    (instruction.narrowLiteral == 0x7f0e06c4 ||
                            instruction.narrowLiteral == 0x7f0b12a2)
        } == true
    }
}

internal val showingAndSearchFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.BRIDGE, AccessFlags.SYNTHETIC)
    returns("V")
}

internal val onClickSearchBarFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;")
    returns("Landroid/view/View;")
    opcodes(
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.INVOKE_VIRTUAL
    )
    strings("try_voice_search", "search_with_your_voice", ",com.google.android.youtube.searchbox=")
}

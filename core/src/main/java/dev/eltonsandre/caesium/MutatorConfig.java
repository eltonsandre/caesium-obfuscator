package dev.eltonsandre.caesium;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MutatorConfig {

    private boolean stringLiteral;
    private boolean controlFlow;
    private boolean number;

    private CaesiumConfig.RemoveOrRename lineNumberTables;
    private CaesiumConfig.RemoveOrRename localVariableTables;
    private CaesiumConfig.ReferenceMutation referenceMutation;

    private boolean polymorph;
    private boolean crasher;
    private boolean classFolder;
    private boolean trimmer;
    private boolean shufflerMembers;

}
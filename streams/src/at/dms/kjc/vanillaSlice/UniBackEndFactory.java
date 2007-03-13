package at.dms.kjc.vanillaSlice;

import at.dms.kjc.backendSupport.BackEndFactory;
import at.dms.kjc.backendSupport.BackEndScaffold;
import at.dms.kjc.backendSupport.ComputeCodeStore;
import at.dms.kjc.backendSupport.ComputeNode;
import at.dms.kjc.backendSupport.ComputeNodes;
import at.dms.kjc.backendSupport.SchedulingPhase;
import at.dms.kjc.slicegraph.Slice;
import at.dms.kjc.slicegraph.InputSliceNode;
import at.dms.kjc.slicegraph.FilterSliceNode;
import at.dms.kjc.slicegraph.OutputSliceNode;
import java.util.*;
import at.dms.kjc.slicegraph.Buffer;
//import at.dms.kjc.spacetime.ComputeNodesI;
/**
 * Stub for uniprocessor backend.
 * @author dimock
 *
 */
public class UniBackEndFactory extends BackEndFactory<
    UniProcessors,
    UniProcessor,
    UniComputeCodeStore,
    Integer> { 

    private UniProcessors processors ;

    /**
     * Constructor if creating UniBackEndFactory before layout
     * Creates <b>numProcessors</b> processors.
     * @param numProcessors  number of processors to create.
     */
    public UniBackEndFactory(Integer numProcessors) {
       this(new UniProcessors(numProcessors));
    }
    
    public UniBackEndFactory() {
        
    }
    
    /**
     * Constructor if creating UniBackEndFactory after layout.
     * Call it with same collection of processors used in Layout.
     * @param processors  the existing collection of processors.
     */
    public UniBackEndFactory(UniProcessors processors) {
        if (processors == null) {
            processors = new UniProcessors(1);
        }
        this.processors = processors;
    }
    
    private BackEndScaffold<UniProcessors, UniProcessor, UniComputeCodeStore, Integer> scaffolding = null;

    @Override
    public  BackEndScaffold<
    UniProcessors, UniProcessor, UniComputeCodeStore, Integer> getBackEndMain() {
        if (scaffolding == null) {
            scaffolding  = new BackEndScaffold<UniProcessors, UniProcessor, UniComputeCodeStore, Integer>();
        }
        return scaffolding;
    }
    
    
    @Override
    public UniProcessors getComputeNodes() {
        return processors;
    }

    
    @Override
    public UniComputeCodeStore getComputeCodeStore(UniProcessor parent) {
        return parent.getComputeCode();
    }

    @Override
    public UniProcessor getComputeNode(Integer specifier) {
        return processors.getNthComputeNode(specifier);
    }
    /**
     * Process an input slice node: find the correct ProcElement(s) and add joiner code, and buffers.
     * please delegate work to some other object.
     * @param input           the InputSliceNode 
     * @param whichPhase      INIT / PRIMEPUMP / STEADY
     * @param rawChip         the available compute nodes.
     * 
     */
    @Override
    public void processInputSliceNode(InputSliceNode input,
            SchedulingPhase whichPhase, UniProcessors computeNodes) {
        
    }
    
    /**
     * Process all filter slice nodes in a Slice (just one in a SimpleSlice): find the correct ProcElement(s) and add filter code.
     * please delegate work to some other object.
     * @param slice           Slice containing filters
     * @param whichPhase      INIT / PRIMEPUMP / STEADY
     * @param computeNodes    the available compute nodes.
     */
    @Override
    public void processFilterSlices(Slice slice, 
            SchedulingPhase whichPhase, UniProcessors computeNodes) {
        
    }

    /**
     * Process a filter slice node: find the correct ProcElement(s) and add code and buffers.
     * please delegate work to some other object.
     * @param filter          the FilterSliceNode.
     * @param whichPhase      INIT / PRIMEPUMP / STEADY
     * @param computeNodes    the available compute nodes.
     */

    public void processFilterSliceNode(FilterSliceNode filter,
            SchedulingPhase whichPhase, UniProcessors computeNodes) {
        
    }

    /**
     * Process an output slice node: find the correct ProcElement(s) and add splitter code, and buffers.
     * please delegate work to some other object.
     * @param output          the OutputSliceNode.
     * @param whichPhase      INIT / PRIMEPUMP / STEADY
     * @param computeNodes    the available compute nodes.
     */
    @Override
    public void processOutputSliceNode(OutputSliceNode output,
            SchedulingPhase whichPhase, UniProcessors computeNodes) {
        
    }
}
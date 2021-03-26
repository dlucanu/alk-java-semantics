package dataflow.wcet;

import dataflow.Lattice;
import dataflow.domain.ProgramContext;
import util.exception.LatticeException;

import java.util.Set;

public class SeqWCETLattice
implements Lattice<ProgramContext>
{
    @Override
    public boolean compare(ProgramContext a, ProgramContext b)
    {
        return false; // TODO: a <= b
    }

    @Override
    public ProgramContext getLUB(Set<ProgramContext> set)
    {
        ProgramContext pc = new ProgramContext();
        for (ProgramContext i : set)
        {
            pc.union(new ProgramContext(i));
        }
        return pc;
    }

    @Override
    public ProgramContext getGLB(Set<ProgramContext> set)
    {
        throw new LatticeException("The GLB of a Sequential WCET Lattice wasn't implemented!");
    }

    @Override
    public ProgramContext getTop()
    {
        throw new LatticeException("The Sequential WCET Lattice doesn't have a top!");
    }

    @Override
    public ProgramContext getBottom()
    {
        return ProgramContext.getEmptyContext();
    }
}
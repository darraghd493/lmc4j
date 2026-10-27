# lmc4j

A clean and simple Java library for working with LMC (Little Man Computer) programs.

> Please see [lmc-interpreter](https://github.com/darraghd493/lmc-interpreter) for my original JavaScript implementation of the LMC instruction set.

## Compatibility

This follows the LMC specification exactly, including the handling of input/output opcodes.

It is nearly _entirely_ compatible with Peter Higginson's LMC simulator, which can be found at [https://peterhigginson.co.uk/lmc/](https://peterhigginson.co.uk/lmc/). We support the non-standard `OTC` instruction (output as character), however, we do not support `.LOC`.


- This directory keeps the source files of the MyScheduler.dex used in the instrumentation.
- The instrumentation does not directly use these source file, but its dex format.

- If you would like to modify these scheduler codes before instrumentation:
  - Compile your modified scheduler java files into a jar file
  - Compile that jar file into dex file using dex2jar tool
   (https://code.google.com/p/dex2jar/)



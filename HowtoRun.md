# How to run #

You can run HTTrack2Arc using an Ant task:

`> ant run -Dsource=<source_dir> -Ddestination=<destination_dir>`

This way, Ant adds the needed dependencies to the class path and
no further configuration is needed.

If don't want to use Ant, make sure to add the needed
dependencies to the classpath and run HTTrack2ARc either
using (from the jar file):

`> java -jar httrack2arc.jar <source_dir> <destination_dir> [--default-time=<default_time>]`

or (from the java class):

`> java HTTrack2ArcConverter <source_dir> <destination_dir> [--default-time=<default_time>]`


  * _source\_dir_: the dir with the HTTrack crawls.
  * _destination\_dir_: the destination for the converted ARC files.
  * _default\_time_: the default time to be given to an archived	file if its date cannot be detected (this should be the date of the HTTrack crawl)

## Limitations ##
  * Changing from _RecursiveCrawlExporter_ to _SingleCrawlExporter_ requires editing _HTTrack2ArcConverter.java_ and recompile.
  * Changing the ARC files _basename_, _prefix_, and default _IP_ requires editing _ArcWriter.java_ and recompile.
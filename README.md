# Java Swing re-implemented for Wayland

![Duke sporting a Wayland logo](res/swingland-duke-wayland.png)

Yes, I'm completely mad.

Yes, I know about [Wakefield](https://wiki.openjdk.org/display/wakefield/OpenJDK+Project+Wakefield+-+Wayland+desktop+support+for+JDK+on+Linux),
and [Caciocavallo](https://github.com/CaciocavalloSilano/caciocavallo), both have been in progress(ish) for years... nothing has arrived in
mainline JDK to fix the internal JDK bugs that prevent a proper Wayland AWT Toolkit. There is
[a horrid fix in Caciocavallo](https://github.com/CaciocavalloSilano/caciocavallo/blob/master/cacio-tta/src/main/java/com/github/caciocavallosilano/cacio/ctc/junit/CacioExtension.java)
which uses reflection to monkey about inside the JDK, and needs *many* command line switches to circumvent security controls...

Yes, I know about XWayland - it's horribly broken for Swing applications (try one!) and is intended as a stop gap, not a long term solution.

## OK.. how?

By re-using the _interfaces_ of Swing, and providing completely new _implementations_ that lean on Wayland and maybe a component toolkit..

Presently, we build a new module / package `com.ashbysoft.swingland`, so client code will have to adjust imports to use us - this is by design
and also avoids horrible pain in replacing a system module (`java.desktop`) which is apparantly impossible without rebuilding the whole JDK..

## Build and test

I've chosen to use [Ant](https://ant.apache.org) as it provides just enough build tooling, is self contained, plus I know how to drive it.

Build/clean:
```bash
% ant [clean|compile|package (default)] [...]
.....
```

Test:
```bash
% ant [test-wayland|test-swingland|test]
.....
```

where the `test-wayland` target directly creates a Wayland window on screen, fills it with random dots for a few seconds and terminates.
The `test-swingland` target runs a Swing GUI application that extends a `JComponent` and draws stuff in the `paintComponent()` method.
It has a working menu bar that demonstrates the popup windows and menuing logic, you can exit via it! `test-swingland` also demonstrates
a couple of pop up `JDialog`s (Swing flavour), one with some `JLabel`s and `JButton`s in a `JSplitPane`, one with a `JTabbedPane`...
more to come as I write it! The `test` target runs both tests.

## Other apps

### Swing demos (in `test.jar`)

I have started importing some of the standard [Swing demos](https://docs.oracle.com/javase/tutorial/uiswing/components/index.html) as additional
test applications, they _should_ work largely unaltered apart from import statements..

Run one:
```bash
% java -cp bin/test.jar com.ashbysoft.test.[TopLevelDemo|ButtonDemo]
```

### Font Editor (in `fed.jar`)

Font EDitor is a dogfooding application written with `swingland` to edit bitmap font files (`res/fonts` and `res/cursors`) used by `swingland`..

Run it:
```bash
% java -jar bin/fed.jar -f <font file> [--help]
```

### Font Display (in `test.jar`)

FontTest was written to test both the PCF font support and font transformations (ie: rotation), run it:
```bash
% java -cp bin/test.jar [--help] [-r] <font name> [...]
```
where `-r` rotates the specified fonts instead of simply printing a line of each, `font name` can be one of: a logical name eg: `MONOSPACED`;
a resource path in the `swingland.jar` file for built-in bitmap fonts eg: `/cursors/CURLY`; a file path for PCF fonts, either absolute or
relative to paths in `XDG_DATA_DIRS` (by default: `/usr/local/share:/usr/share`), eg: `/path/to/font.pcf`, `X11/fonts/misc/10x20.pcf.gz`.

### More Dogfood..

I've ported my own applications to Swingland, noteably:
 * [java-sdr](https://github.com/phlash/java-sdr) My pure Java software defined radio toy..
 * [powermonitor](https://github.com/phlash/powermonitor) The two data display apps for my home electricity monitoring setup

## Licence

See [LICENCE.md](LICENCE.md)

## Blog

[I wrote up my development experience](https://www.ashbysoft.com/articles/swingland/)
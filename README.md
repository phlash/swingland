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
a simple pop up `JDialog` (Swing flavour), with some `JLabel`s and `JButton`s in... more to come as I write it!
The `test` target runs both tests.


## Licence

See [LICENCE.md](LICENCE.md)

# Storm Blue + Liquid Glass Theme

Primary colour: **Storm Blue `#34506D`** (`Theme.STORM_BLUE` / `Theme.PRIMARY`).

All styling lives in `src/com/fams/gui/Theme.java`.

| Constant | Value | Use |
|---|---|---|
| `PRIMARY` | `#34506D` | Buttons, badges, top bar |
| `PRIMARY_DARK` | `#22374D` | Table headers, pressed state, headings |
| `PRIMARY_LIGHT` | `#5E82A8` | Selection, accents |
| `LIGHT_BG` | `#E6EEF6` | Key colour for transparent panels |
| `TABLE_ROW_A/B`, `TABLE_SELECT` | light blue tints | Table rows and selection |

## Liquid glass
Swing cannot blur what is behind a component, so the effect is built from:
a soft Storm Blue gradient backdrop (`Theme.BackdropPanel`), translucent rounded panels
(`Theme.GlassPanel`, `Theme.createCardPanel()`), a specular top highlight, a light edge stroke
and a soft shadow. Buttons (`GlassButtonUI`) and tabs (`GlassTabbedPaneUI`) use the same treatment.

## Using it in new screens
- Cards: `Theme.createCardPanel()`
- Buttons: `Theme.createPrimaryButton / createSecondaryButton / createDangerButton / createSuccessButton`
- Window root: `new Theme.BackdropPanel(new BorderLayout())`
- Call `Theme.applyGlass(rootComponent)` after building a screen: plain panels using `Theme.LIGHT_BG` become
  transparent and white panels become glass cards.
- Status/importance edge on a glass card: `card.putClientProperty("fams.glassEdge", Theme.DANGER)`

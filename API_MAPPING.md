# ftxui ↔ ftxui-kt API Mapping

Maps every public symbol in the ftxui C++ library to its ftxui-kt Kotlin equivalent.

**Status key**
- ✅ Implemented — full parity
- ⚠️ Partial — present but with missing overloads or reduced functionality
- ❌ Missing — not exposed in ftxui-kt
- — Not applicable — design difference (C++ plumbing with no Kotlin equivalent needed)

---

## Table of Contents

1. [Screen Module](#screen-module)
   - [Color](#color)
   - [ColorInfo](#colorinfo)
   - [Terminal](#terminal)
   - [Box / Cell / Surface / Screen (low-level)](#box--cell--surface--screen-low-level)
2. [DOM Module](#dom-module)
   - [Element widget constructors](#element-widget-constructors)
   - [Element decorators](#element-decorators)
   - [Canvas](#canvas)
   - [FlexboxConfig](#flexboxconfig)
   - [LinearGradient](#lineargradient)
   - [Table & TableSelection](#table--tableselection)
3. [Component Module](#component-module)
   - [App (FtxUIApp)](#app-ftxuiapp)
   - [Loop (FtxUILoop)](#loop-ftxuiloop)
   - [Event & Key constants](#event--key-constants)
   - [Mouse](#mouse)
   - [Animation](#animation)
   - [ComponentBase](#componentbase)
   - [Container functions](#container-functions)
   - [Component factories](#component-factories)
   - [Component options structs](#component-options-structs)
4. [Util Module](#util-module)
5. [Summary](#summary)

---

## Screen Module

### Color

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Color()` (transparent) | internal constructor | — | Not user-constructable directly |
| `Color(Palette1::Default)` | `Color.Default` | ✅ | |
| `Color(Palette16::Black)` … `White` | `Color.Black` … `Color.White` | ✅ | All 16 palette colors as companion properties |
| `Color(Palette256 index)` | `Color.palette256(index: Int)` | ⚠️ | C++ uses typed `Palette256` enum; Kotlin uses raw `Int` |
| `Color(r, g, b, a=255)` | `Color.rgb(r,g,b)` / `Color.rgba(r,g,b,a)` | ✅ | |
| `Color::RGB(r,g,b)` | `Color.rgb(r,g,b)` | ✅ | |
| `Color::HSV(h,s,v)` | `Color.hsv(h,s,v)` | ✅ | |
| `Color::RGBA(r,g,b,a)` | `Color.rgba(r,g,b,a)` | ✅ | |
| `Color::HSVA(h,s,v,a)` | `Color.hsva(h,s,v,a)` | ✅ | |
| `Color::Interpolate(t, a, b)` | `Color.interpolate(ratio, a, b)` | ✅ | |
| `Color::Blend(lhs, rhs)` | `Color.blend(lhs, rhs)` | ✅ | |
| `Color::operator==(rhs)` | `Color.equals(other)` | ✅ | |
| `Color::operator!=(rhs)` | `Color.equals(other)` (via `!=`) | ✅ | |
| `Color::Print(is_background)` | `Color.print(isBackground)` | ✅ | |
| `Color::IsOpaque()` | `Color.isOpaque()` | ✅ | |
| `Color::palette1(index)` | `Color.palette1(index)` | ✅ | |
| `Color::palette16(index)` | `Color.palette16(index)` | ✅ | |
| `operator""_rgb(combined)` | — | ❌ | No Kotlin literal-suffix equivalent |

### ColorInfo

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `ColorInfo.name` | `ColorInfo.name` | ✅ | |
| `ColorInfo.index_256` | `ColorInfo.index256` | ✅ | |
| `ColorInfo.index_16` | `ColorInfo.index16` | ✅ | |
| `ColorInfo.red` | `ColorInfo.red` | ✅ | |
| `ColorInfo.green` | `ColorInfo.green` | ✅ | |
| `ColorInfo.blue` | `ColorInfo.blue` | ✅ | |
| `ColorInfo.hue` | `ColorInfo.hue` | ✅ | |
| `ColorInfo.saturation` | `ColorInfo.saturation` | ✅ | |
| `ColorInfo.value` | `ColorInfo.value` | ✅ | |
| `GetColorInfo(Palette256)` | `getColorInfo256(index)` | ✅ | Renamed to avoid overload clash (both palette types are `UInt`) |
| `GetColorInfo(Palette16)` | `getColorInfo16(index)` | ✅ | |
| `ColorInfoSorted2D()` | `colorInfoSorted2D()` | ✅ | |

### Terminal

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Terminal::Size()` | `Terminal.size()` | ✅ | Returns `Dimensions(dimx, dimy)` |
| `Terminal::SetFallbackSize(dims)` | `Terminal.setFallbackSize(w, h)` | ✅ | |
| `Terminal::ColorSupport()` | `Terminal.colorSupport()` | ✅ | Returns `TerminalColor` enum |
| `Terminal::SetColorSupport(color)` | `Terminal.setColorSupport(color)` | ✅ | |
| `Terminal::Color` enum (Palette1/16/256/TrueColor) | `TerminalColor` enum | ✅ | |
| `Terminal::Quirks` class | `Quirks` data class | ✅ | |
| `Terminal::GetQuirks()` | `Terminal.getQuirks()` | ✅ | Returns `Quirks` |
| `Terminal::SetQuirks(quirks)` | `Terminal.setQuirks(quirks)` | ✅ | |
| `Terminal::TerminalInfo` class | — | ❌ | Not exposed |
| `Terminal::ComputeColorSupport(...)` | — | ❌ | Not exposed |
| `Dimensions::Fixed(int)` | — | ❌ | Not exposed |
| `Dimensions::Full()` | — | ❌ | Not exposed |

### Box / Cell / Surface / Screen (low-level)

These are internal rendering primitives. Omitting them from the Kotlin API is intentional; they are noted here for completeness.

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Box` struct | — | — | Internal; not needed for API parity |
| `Cell` struct | — | — | Internal; used indirectly via `selectionStyle` C callback |
| `Surface` class | — | — | Internal |
| `Screen` class | — | — | Internal |
| `Image` (alias for `Surface`) | — | — | Deprecated alias; internal |
| `Pixel` (alias for `Cell`) | — | — | Deprecated alias; internal |
| `string_width(sv)` | — | — | Kotlin stdlib handles string metrics |
| `Utf8ToGlyphs(sv)` | — | — | Not exposed |
| `CellToGlyphIndex(sv)` | — | — | Not exposed |
| `to_string(wstring_view)` | — | — | Not needed in Kotlin |
| `to_wstring(string_view)` | — | — | Not needed in Kotlin |

---

## DOM Module

### Element widget constructors

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `text(sv)` | `text(text: String)` | ✅ | |
| `vtext(sv)` | `vtext(text: String)` | ✅ | |
| `separator()` | `separator()` | ✅ | |
| `separatorLight()` | `separatorLight()` | ✅ | |
| `separatorDashed()` | `separatorDashed()` | ✅ | |
| `separatorHeavy()` | `separatorHeavy()` | ✅ | |
| `separatorDouble()` | `separatorDouble()` | ✅ | |
| `separatorEmpty()` | `separatorEmpty()` | ✅ | |
| `separatorStyled(BorderStyle)` | `separatorStyled(style: BorderStyle)` | ✅ | |
| `separator(Cell)` | — | ❌ | `Cell` not exposed |
| `separatorCharacter(sv)` | `separatorCharacter(character: String)` | ✅ | |
| `separatorHSelector(l, r, uc, sc)` | `separatorHSelector(left, right, unselectedColor, selectedColor)` | ✅ | |
| `separatorVSelector(u, d, uc, sc)` | `separatorVSelector(up, down, unselectedColor, selectedColor)` | ✅ | |
| `gauge(progress)` | `gauge(value: Float)` | ✅ | |
| `gaugeLeft(progress)` | `gaugeLeft(value: Float)` | ✅ | |
| `gaugeRight(progress)` | `gaugeRight(value: Float)` | ✅ | |
| `gaugeUp(progress)` | `gaugeUp(value: Float)` | ✅ | |
| `gaugeDown(progress)` | `gaugeDown(value: Float)` | ✅ | |
| `gaugeDirection(progress, dir)` | `gaugeDirection(value, direction)` | ✅ | |
| `border(Element)` | `Element.border()` | ✅ | |
| `borderLight(Element)` | `Element.borderLight()` | ✅ | |
| `borderDashed(Element)` | `Element.borderDashed()` | ✅ | |
| `borderHeavy(Element)` | `Element.borderHeavy()` | ✅ | |
| `borderDouble(Element)` | `Element.borderDouble()` | ✅ | |
| `borderRounded(Element)` | `Element.borderRounded()` | ✅ | |
| `borderEmpty(Element)` | `Element.borderEmpty()` | ✅ | |
| `borderStyled(BorderStyle)` | `Element.borderStyled(style)` | ✅ | |
| `borderStyled(BorderStyle, Color)` | `Element.borderStyled(style, color)` | ✅ | |
| `borderStyled(Color)` | `Element.borderStyled(color)` | ✅ | |
| `borderWith(Cell)` | — | ❌ | `Cell` not exposed |
| `window(title, content, border=ROUNDED)` | `Element.window(title)` | ⚠️ | `BorderStyle` parameter missing; always uses default |
| `spinner(charset_index, image_index)` | `spinner(charsetIndex, imageIndex)` | ✅ | |
| `paragraph(sv)` | `paragraph(text: String)` | ✅ | |
| `paragraphAlignLeft(sv)` | `paragraphAlignLeft(text)` | ✅ | |
| `paragraphAlignRight(sv)` | `paragraphAlignRight(text)` | ✅ | |
| `paragraphAlignCenter(sv)` | `paragraphAlignCenter(text)` | ✅ | |
| `paragraphAlignJustify(sv)` | `paragraphAlignJustify(text)` | ✅ | |
| `graph(GraphFunction)` | `graph(fn: GraphFn)` | ⚠️ | Requires `GraphFn` wrapper class; no direct lambda form |
| `emptyElement()` | `emptyElement()` | ✅ | |
| `canvas(ConstRef<Canvas>)` | `Canvas.toElement()` | ✅ | Different invocation style |
| `canvas(width, height, fn)` | — | ❌ | Inline lambda form not exposed |
| `canvas(fn)` | — | ❌ | Inline lambda form not exposed |
| `hbox(Elements)` | `hbox(vararg elements: Element)` | ✅ | |
| `vbox(Elements)` | `vbox(vararg elements: Element)` | ✅ | |
| `dbox(Elements)` | `dbox(vararg elements: Element)` | ✅ | |
| `flexbox(Elements, FlexboxConfig)` | `flexbox(vararg elements, config)` | ✅ | |
| `gridbox(vector<Elements>)` | `gridbox(rows: List<List<Element>>)` | ✅ | |
| `hflow(Elements)` | `hflow(vararg elements: Element)` | ✅ | |
| `vflow(Elements)` | `vflow(vararg elements: Element)` | ✅ | |
| `filler()` | `filler()` | ✅ | |

### Element decorators

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `bold(Element)` | `Element.bold()` | ✅ | |
| `dim(Element)` | `Element.dim()` | ✅ | |
| `italic(Element)` | `Element.italic()` | ✅ | |
| `inverted(Element)` | `Element.inverted()` | ✅ | |
| `underlined(Element)` | `Element.underlined()` | ✅ | |
| `underlinedDouble(Element)` | `Element.underlinedDouble()` | ✅ | |
| `blink(Element)` | `Element.blink()` | ✅ | |
| `strikethrough(Element)` | `Element.strikethrough()` | ✅ | |
| `color(Color)(Element)` | `Element.color(color)` | ✅ | |
| `bgcolor(Color)(Element)` | `Element.bgcolor(color)` | ✅ | |
| `color(LinearGradient)(Element)` | `Element.colorLinearGradient(gradient)` | ✅ | |
| `bgcolor(LinearGradient)(Element)` | `Element.bgcolorLinearGradient(gradient)` | ✅ | |
| `focusPosition(x, y)` | `Element.focusPosition(x, y)` | ✅ | |
| `focusPositionRelative(x, y)` | `Element.focusPositionRelative(x, y)` | ✅ | |
| `automerge(Element)` | `Element.automerge()` | ✅ | |
| `hyperlink(link)(Element)` | `Element.hyperlink(link)` | ✅ | |
| `selectionStyleReset(Element)` | `Element.selectionStyleReset()` | ✅ | |
| `selectionColor(Color)(Element)` | `Element.selectionColor(color)` | ✅ | |
| `selectionBackgroundColor(Color)(Element)` | `Element.selectionBgColor(color)` | ✅ | |
| `selectionForegroundColor(Color)(Element)` | `Element.selectionFgColor(color)` | ✅ | |
| `selectionStyle(fn<void(Cell&)>)(Element)` | `Element.selectionStyle(callback, userdata)` | ⚠️ | Requires raw C callback pointer; no Kotlin lambda form |
| `flex(Element)` | `Element.flex()` | ✅ | |
| `flex_grow(Element)` | `Element.flexGrow()` | ✅ | |
| `flex_shrink(Element)` | `Element.flexShrink()` | ✅ | |
| `xflex(Element)` | `Element.xflex()` | ✅ | |
| `xflex_grow(Element)` | `Element.xflexGrow()` | ✅ | |
| `xflex_shrink(Element)` | `Element.xflexShrink()` | ✅ | |
| `yflex(Element)` | `Element.yflex()` | ✅ | |
| `yflex_grow(Element)` | `Element.yflexGrow()` | ✅ | |
| `yflex_shrink(Element)` | `Element.yflexShrink()` | ✅ | |
| `notflex(Element)` | `Element.notflex()` | ✅ | |
| `size(WidthOrHeight, Constraint, int)` | `Element.size(widthOrHeight, constraint, value)` | ✅ | |
| `frame(Element)` | `Element.frame()` | ✅ | |
| `xframe(Element)` | `Element.xframe()` | ✅ | |
| `yframe(Element)` | `Element.yframe()` | ✅ | |
| `focus(Element)` | `Element.focus()` | ✅ | |
| `select(Element)` | — | — | Deprecated alias for `focus`; intentionally omitted |
| `focusCursorBlock(Element)` | `Element.focusCursorBlock()` | ✅ | |
| `focusCursorBlockBlinking(Element)` | `Element.focusCursorBlockBlinking()` | ✅ | |
| `focusCursorBar(Element)` | `Element.focusCursorBar()` | ✅ | |
| `focusCursorBarBlinking(Element)` | `Element.focusCursorBarBlinking()` | ✅ | |
| `focusCursorUnderline(Element)` | `Element.focusCursorUnderline()` | ✅ | |
| `focusCursorUnderlineBlinking(Element)` | `Element.focusCursorUnderlineBlinking()` | ✅ | |
| `vscroll_indicator(Element)` | `Element.vscrollIndicator()` | ✅ | |
| `hscroll_indicator(Element)` | `Element.hscrollIndicator()` | ✅ | |
| `reflect(Box&)(Element)` | — | ❌ | `Box` not exposed |
| `clear_under(Element)` | `Element.clearUnder()` | ✅ | |
| `hcenter(Element)` | `Element.hcenter()` | ✅ | |
| `vcenter(Element)` | `Element.vcenter()` | ✅ | |
| `center(Element)` | `Element.center()` | ✅ | |
| `align_right(Element)` | `Element.alignRight()` | ✅ | |
| `nothing(Element)` | `Element.nothing()` | ✅ | |
| `Dimensions Fit(Element&, bool)` | — | ❌ | Not exposed |
| `operator\|(Element, Decorator)` | — | — | Kotlin uses extension functions directly |

### Canvas

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Canvas()` | — | ❌ | Default (zero-size) constructor not exposed |
| `Canvas(width, height)` | `Canvas(width, height)` | ✅ | Via companion `operator fun invoke` |
| `Canvas::width()` | `Canvas.width()` | ✅ | |
| `Canvas::height()` | `Canvas.height()` | ✅ | |
| `Canvas::GetCell(x, y)` | — | ❌ | `Cell` not exposed |
| `Canvas::DrawPointOn(x, y)` | `Canvas.drawPointOn(x, y)` | ✅ | |
| `Canvas::DrawPointOff(x, y)` | `Canvas.drawPointOff(x, y)` | ✅ | |
| `Canvas::DrawPointToggle(x, y)` | `Canvas.drawPointToggle(x, y)` | ✅ | |
| `Canvas::DrawPoint(x, y, value)` | `Canvas.drawPoint(x, y, value)` | ✅ | |
| `Canvas::DrawPoint(x, y, value, Color)` | `Canvas.drawPoint(x, y, value, color)` | ✅ | |
| `Canvas::DrawPoint(x, y, value, Stylizer)` | — | ❌ | Stylizer form not exposed |
| `Canvas::DrawPointLine(x1,y1,x2,y2)` | `Canvas.drawPointLine(x1,y1,x2,y2,null)` | ⚠️ | No standalone no-color overload; pass `null` for color |
| `Canvas::DrawPointLine(x1,y1,x2,y2,Color)` | `Canvas.drawPointLine(x1,y1,x2,y2,color)` | ✅ | |
| `Canvas::DrawPointLine(x1,y1,x2,y2,Stylizer)` | — | ❌ | Stylizer form not exposed |
| `Canvas::DrawPointCircle(x,y,r)` | `Canvas.drawPointCircle(x,y,radius)` | ✅ | |
| `Canvas::DrawPointCircle(x,y,r,Color)` | `Canvas.drawPointCircle(x,y,radius,color)` | ✅ | |
| `Canvas::DrawPointCircle(x,y,r,Stylizer)` | — | ❌ | |
| `Canvas::DrawPointCircleFilled(x,y,r)` | `Canvas.drawPointCircleFilled(x,y,radius)` | ✅ | |
| `Canvas::DrawPointCircleFilled(x,y,r,Color)` | `Canvas.drawPointCircleFilled(x,y,radius,color)` | ✅ | |
| `Canvas::DrawPointCircleFilled(x,y,r,Stylizer)` | — | ❌ | |
| `Canvas::DrawPointEllipse(x,y,r1,r2)` | `Canvas.drawPointEllipse(x,y,rx,ry)` | ✅ | |
| `Canvas::DrawPointEllipse(x,y,r1,r2,Color)` | `Canvas.drawPointEllipse(x,y,rx,ry,color)` | ✅ | |
| `Canvas::DrawPointEllipse(x,y,r1,r2,Stylizer)` | — | ❌ | |
| `Canvas::DrawPointEllipseFilled(x,y,r1,r2)` | `Canvas.drawPointEllipseFilled(x,y,rx,ry)` | ✅ | |
| `Canvas::DrawPointEllipseFilled(x,y,r1,r2,Color)` | `Canvas.drawPointEllipseFilled(x,y,rx,ry,color)` | ✅ | |
| `Canvas::DrawPointEllipseFilled(x,y,r1,r2,Stylizer)` | — | ❌ | |
| `Canvas::DrawBlockOn(x,y)` | `Canvas.drawBlockOn(x, y)` | ✅ | |
| `Canvas::DrawBlockOff(x,y)` | `Canvas.drawBlockOff(x, y)` | ✅ | |
| `Canvas::DrawBlockToggle(x,y)` | `Canvas.drawBlockToggle(x, y)` | ✅ | |
| `Canvas::DrawBlock(x,y,value)` | `Canvas.drawBlock(x, y, value)` | ✅ | |
| `Canvas::DrawBlock(x,y,value,Color)` | `Canvas.drawBlock(x, y, value, color)` | ✅ | |
| `Canvas::DrawBlock(x,y,value,Stylizer)` | — | ❌ | |
| `Canvas::DrawBlockLine(x1,y1,x2,y2)` | `Canvas.drawBlockLine(x1,y1,x2,y2,null)` | ⚠️ | No standalone no-color overload; pass `null` for color |
| `Canvas::DrawBlockLine(x1,y1,x2,y2,Color)` | `Canvas.drawBlockLine(x1,y1,x2,y2,color)` | ✅ | |
| `Canvas::DrawBlockLine(x1,y1,x2,y2,Stylizer)` | — | ❌ | |
| `Canvas::DrawBlockCircle(x,y,r)` | `Canvas.drawBlockCircle(x,y,radius)` | ✅ | |
| `Canvas::DrawBlockCircle(x,y,r,Color)` | `Canvas.drawBlockCircle(x,y,radius,color)` | ✅ | |
| `Canvas::DrawBlockCircle(x,y,r,Stylizer)` | — | ❌ | |
| `Canvas::DrawBlockCircleFilled(x,y,r)` | `Canvas.drawBlockCircleFilled(x,y,radius)` | ✅ | |
| `Canvas::DrawBlockCircleFilled(x,y,r,Color)` | `Canvas.drawBlockCircleFilled(x,y,radius,color)` | ✅ | |
| `Canvas::DrawBlockCircleFilled(x,y,r,Stylizer)` | — | ❌ | |
| `Canvas::DrawBlockEllipse(x,y,r1,r2)` | `Canvas.drawBlockEllipse(x,y,rx,ry)` | ✅ | |
| `Canvas::DrawBlockEllipse(x,y,r1,r2,Color)` | `Canvas.drawBlockEllipse(x,y,rx,ry,color)` | ✅ | |
| `Canvas::DrawBlockEllipse(x,y,r1,r2,Stylizer)` | — | ❌ | |
| `Canvas::DrawBlockEllipseFilled(x,y,r1,r2)` | `Canvas.drawBlockEllipseFilled(x,y,rx,ry)` | ✅ | |
| `Canvas::DrawBlockEllipseFilled(x,y,r1,r2,Color)` | `Canvas.drawBlockEllipseFilled(x,y,rx,ry,color)` | ✅ | |
| `Canvas::DrawBlockEllipseFilled(x,y,r1,r2,Stylizer)` | — | ❌ | |
| `Canvas::DrawText(x,y,sv)` | `Canvas.drawText(x,y,text)` | ✅ | |
| `Canvas::DrawText(x,y,sv,Color)` | `Canvas.drawText(x,y,text,color)` | ✅ | |
| `Canvas::DrawText(x,y,sv,Stylizer)` | — | ❌ | Stylizer form not exposed |
| `Canvas::DrawCell(x,y,Cell)` | — | ❌ | `Cell` not exposed |
| `Canvas::DrawSurface(x,y,Surface)` | — | ❌ | `Surface` not exposed |
| `Canvas::Style(x,y,Stylizer)` | `Canvas.style(x, y, callback, userdata)` | ⚠️ | Exposed but requires raw C callback; no Kotlin lambda form |

### FlexboxConfig

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `FlexboxConfig::direction` | `FlexboxConfig.direction` | ✅ | |
| `FlexboxConfig::wrap` | `FlexboxConfig.wrap` | ✅ | |
| `FlexboxConfig::justify_content` | `FlexboxConfig.justifyContent` | ✅ | |
| `FlexboxConfig::align_items` | `FlexboxConfig.alignItems` | ✅ | |
| `FlexboxConfig::align_content` | `FlexboxConfig.alignContent` | ✅ | |
| `FlexboxConfig::gap_x` | `FlexboxConfig.gapX` | ✅ | |
| `FlexboxConfig::gap_y` | `FlexboxConfig.gapY` | ✅ | |
| `FlexboxConfig::Set(Direction)` | — | ❌ | No fluent builder methods; set fields in constructor |
| `FlexboxConfig::Set(Wrap)` | — | ❌ | |
| `FlexboxConfig::Set(JustifyContent)` | — | ❌ | |
| `FlexboxConfig::Set(AlignItems)` | — | ❌ | |
| `FlexboxConfig::Set(AlignContent)` | — | ❌ | |
| `FlexboxConfig::SetGap(x, y)` | — | ❌ | |

### LinearGradient

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `LinearGradient()` | `LinearGradient()` | ✅ | |
| `LinearGradient(begin, end)` | `LinearGradient(begin, end)` | ✅ | |
| `LinearGradient(angle, begin, end)` | `LinearGradient(angle, begin, end)` | ✅ | |
| `LinearGradient::Angle(float)` | `LinearGradient.angle(degrees)` | ✅ | |
| `LinearGradient::Stop(Color, float)` | `LinearGradient.stop(color, position)` | ✅ | |
| `LinearGradient::Stop(Color)` | `LinearGradient.stop(color)` | ✅ | |

### Table & TableSelection

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Table()` | — | ❌ | Default constructor not exposed |
| `Table(vector<vector<string>>)` | `Table(rows: List<List<String>>)` | ✅ | |
| `Table(vector<vector<Element>>)` | — | ❌ | Element-based table not exposed |
| `Table::SelectAll()` | `Table.selectAll()` | ✅ | |
| `Table::SelectCell(col, row)` | `Table.selectCell(col, row)` | ✅ | |
| `Table::SelectRow(row)` | `Table.selectRow(row)` | ✅ | |
| `Table::SelectRows(min, max)` | `Table.selectRows(from, to)` | ✅ | |
| `Table::SelectColumn(col)` | `Table.selectColumn(col)` | ✅ | |
| `Table::SelectColumns(min, max)` | `Table.selectColumns(from, to)` | ✅ | |
| `Table::SelectRectangle(c_min,c_max,r_min,r_max)` | `Table.selectRectangle(colMin,colMax,rowMin,rowMax)` | ✅ | |
| `Table::Render()` | `Table.render()` | ✅ | |
| `TableSelection::Decorate(Decorator)` | `TableSelection.decorate(transform)` | ✅ | Kotlin `(Element)->Element` lambda |
| `TableSelection::DecorateAlternateRow(Decorator,modulo,shift)` | `TableSelection.decorateAlternateRow(transform,modulo,shift)` | ✅ | |
| `TableSelection::DecorateAlternateColumn(Decorator,modulo,shift)` | `TableSelection.decorateAlternateColumn(transform,modulo,shift)` | ✅ | |
| `TableSelection::DecorateCells(Decorator)` | `TableSelection.decorateCells(transform)` | ✅ | |
| `TableSelection::DecorateCellsAlternateColumn(Decorator,modulo,shift)` | `TableSelection.decorateCellsAlternateColumn(transform,modulo,shift)` | ✅ | |
| `TableSelection::DecorateCellsAlternateRow(Decorator,modulo,shift)` | `TableSelection.decorateCellsAlternateRow(transform,modulo,shift)` | ✅ | |
| `TableSelection::DecorateBorder(Decorator)` | `TableSelection.decorateBorder(transform)` | ✅ | |
| `TableSelection::DecorateBorderLeft(Decorator)` | `TableSelection.decorateBorderLeft(transform)` | ✅ | |
| `TableSelection::DecorateBorderRight(Decorator)` | `TableSelection.decorateBorderRight(transform)` | ✅ | |
| `TableSelection::DecorateBorderTop(Decorator)` | `TableSelection.decorateBorderTop(transform)` | ✅ | |
| `TableSelection::DecorateBorderBottom(Decorator)` | `TableSelection.decorateBorderBottom(transform)` | ✅ | |
| `TableSelection::DecorateSeparator(Decorator)` | `TableSelection.decorateSeparator(transform)` | ✅ | |
| `TableSelection::DecorateSeparatorVertical(Decorator)` | `TableSelection.decorateSeparatorVertical(transform)` | ✅ | |
| `TableSelection::DecorateSeparatorHorizontal(Decorator)` | `TableSelection.decorateSeparatorHorizontal(transform)` | ✅ | |
| `TableSelection::Border(BorderStyle)` | `TableSelection.border(style)` | ✅ | |
| `TableSelection::Border(BorderStyle, Decorator)` | `TableSelection.borderColor(style, color)` | ⚠️ | Only color form; no generic decorator overload |
| `TableSelection::BorderLeft(BorderStyle)` | `TableSelection.borderLeft(style)` | ✅ | |
| `TableSelection::BorderRight(BorderStyle)` | `TableSelection.borderRight(style)` | ✅ | |
| `TableSelection::BorderTop(BorderStyle)` | `TableSelection.borderTop(style)` | ✅ | |
| `TableSelection::BorderBottom(BorderStyle)` | `TableSelection.borderBottom(style)` | ✅ | |
| `TableSelection::Separator(BorderStyle)` | `TableSelection.separator(style)` | ✅ | |
| `TableSelection::Separator(BorderStyle, Decorator)` | — | ❌ | Decorator overload of separator not exposed |
| `TableSelection::SeparatorVertical(BorderStyle)` | `TableSelection.separatorVertical(style)` | ✅ | |
| `TableSelection::SeparatorHorizontal(BorderStyle)` | `TableSelection.separatorHorizontal(style)` | ✅ | |
| `TableSelection` — `bold` cell decorator | `TableSelection.decorateBold()` | ✅ | Convenience helper |
| `TableSelection` — `align_right` cell decorator | `TableSelection.decorateCellsAlignRight()` | ✅ | |
| `TableSelection` — `color(c)` cell decorator | `TableSelection.decorateCellsColor(color)` | ✅ | |

---

## Component Module

### App (FtxUIApp)

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `App::FixedSize(dimx, dimy)` | `FtxUIApp.fixedSize(dimx, dimy)` | ✅ | |
| `App::Fullscreen()` | `FtxUIApp.fullscreen()` | ✅ | |
| `App::FullscreenPrimaryScreen()` | `FtxUIApp.fullscreenPrimaryScreen()` | ✅ | |
| `App::FullscreenAlternateScreen()` | `FtxUIApp.fullscreenAlternateScreen()` | ✅ | |
| `App::FitComponent()` | `FtxUIApp.fitComponent()` | ✅ | |
| `App::TerminalOutput()` | `FtxUIApp.terminalOutput()` | ✅ | |
| `App::Active()` | — | ❌ | Static accessor; not exposed |
| `App::TrackMouse(bool)` | `FtxUIApp.trackMouse(enable)` | ✅ | |
| `App::HandlePipedInput(bool)` | `FtxUIApp.handlePipedInput(enable)` | ✅ | |
| `App::Loop(Component)` | `FtxUIApp.loop(root)` | ✅ | |
| `App::Exit()` | `FtxUIApp.exit()` | ✅ | |
| `App::ExitLoopClosure()` | `FtxUIApp.exitClosure()` | ✅ | |
| `App::WithRestoredIO(Closure)` | `FtxUIApp.withRestoredIO(closure)` | ✅ | |
| `App::ForceHandleCtrlC(bool)` | `FtxUIApp.forceHandleCtrlC(force)` | ✅ | |
| `App::ForceHandleCtrlZ(bool)` | `FtxUIApp.forceHandleCtrlZ(force)` | ✅ | |
| `App::Post(Task)` | `FtxUIApp.post(closure)` | ⚠️ | Only closure form; no Task/Event variant |
| `App::PostEvent(Event)` | — | ❌ | No user-constructable Event handle |
| `App::PostEventOrExecute(Closure)` | — | ❌ | Not exposed |
| `App::RequestAnimationFrame()` | `FtxUIApp.requestAnimationFrame()` | ✅ | Extension function |
| `App::CaptureMouse()` | — | ❌ | Not exposed |
| `App::GetSelection()` | `FtxUIApp.getSelection()` | ✅ | |
| `App::SelectionChange(callback)` | `FtxUIApp.selectionChange(callback)` | ✅ | |
| `App::TerminalName()` | `FtxUIApp.terminalName()` | ✅ | |
| `App::TerminalVersion()` | `FtxUIApp.terminalVersion()` | ✅ | |
| `App::TerminalEmulatorName()` | `FtxUIApp.terminalEmulatorName()` | ✅ | |
| `App::TerminalEmulatorVersion()` | `FtxUIApp.terminalEmulatorVersion()` | ✅ | |
| `App::TerminalCapabilities()` | `FtxUIApp.terminalCapabilities()` | ✅ | Returns `List<Int>` |
| `App::TerminalCapabilityNames()` | — | ❌ | No C API for name lookup |
| `App::Active()` | `FtxUIApp.active()` | ✅ | Returns `FtxUIApp?` |

### Loop (FtxUILoop)

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Loop(App*, Component)` | `FtxUILoop(app, component)` | ✅ | |
| `Loop::HasQuitted()` | `FtxUILoop.hasQuitted()` | ✅ | |
| `Loop::RunOnce()` | `FtxUILoop.runOnce()` | ✅ | |
| `Loop::RunOnceBlocking()` | `FtxUILoop.runOnceBlocking()` | ✅ | |
| `Loop::Run()` | `FtxUILoop.run()` | ✅ | Implemented as `while (!hasQuitted()) runOnceBlocking()` |

### Event & Key constants

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Event::Character(sv)` | — | ❌ | Events are only received, not user-constructed |
| `Event::Special(sv)` | — | ❌ | |
| `Event::Mouse(sv, Mouse)` | — | ❌ | |
| `Event::ArrowLeft/Right/Up/Down` | `Key.ArrowLeft/Right/Up/Down` | ✅ | Used with `FtxUIEvent.isKey()` |
| `Event::ArrowLeftCtrl` … `ArrowDownCtrl` | `Key.ArrowLeftCtrl` … `Key.ArrowDownCtrl` | ✅ | |
| `Event::Backspace` | `Key.Backspace` | ✅ | |
| `Event::Delete` | `Key.Delete` | ✅ | |
| `Event::Return` | `Key.Return` | ✅ | |
| `Event::Escape` | `Key.Escape` | ✅ | |
| `Event::Tab` / `TabReverse` | `Key.Tab` / `Key.TabReverse` | ✅ | |
| `Event::Insert/Home/End/PageUp/PageDown` | `Key.Insert` … `Key.PageDown` | ✅ | |
| `Event::F1` … `F12` | `Key.F1` … `Key.F12` | ✅ | |
| `Event::CtrlA` … `CtrlZ` | `Key.CtrlA` … `Key.CtrlZ` | ✅ | |
| `Event::AltA` … `AltZ` | `Key.AltA` … `Key.AltZ` | ✅ | |
| `Event::CtrlAltA` … `CtrlAltZ` | `Key.CtrlAltA` … `Key.CtrlAltZ` | ✅ | |
| `Event::input()` | `FtxUIEvent.input` | ✅ | |
| `Event::is_character()` | `FtxUIEvent.isCharacter` | ✅ | |
| `Event::character()` | `FtxUIEvent.character` | ✅ | |
| `Event::is_mouse()` | `FtxUIEvent.isMouse` | ✅ | |
| `Event::mouse().x` | `FtxUIEvent.mouseX` | ✅ | |
| `Event::mouse().y` | `FtxUIEvent.mouseY` | ✅ | |
| `Event::mouse().button` | — | ❌ | Mouse button not exposed |
| `Event::mouse().motion` | — | ❌ | Mouse motion (pressed/released/moved) not exposed |
| `Event::mouse().shift` | — | ❌ | Mouse modifier keys not exposed |
| `Event::mouse().meta` | — | ❌ | |
| `Event::mouse().control` | — | ❌ | |
| `Event::is_cursor_position()` | `FtxUIEvent.isCursorPosition` | ✅ | |
| `Event::cursor_x()` / `cursor_y()` | `FtxUIEvent.cursorX` / `cursorY` | ✅ | |
| `Event::is_cursor_shape()` | `FtxUIEvent.isCursorShape` | ✅ | |
| `Event::cursor_shape()` | `FtxUIEvent.cursorShape` | ✅ | |
| `Event::IsTerminalNameVersion()` | `FtxUIEvent.isTerminalNameVersion` | ✅ | |
| `Event::TerminalName()` | `FtxUIEvent.terminalName` | ✅ | |
| `Event::TerminalVersion()` | `FtxUIEvent.terminalVersion` | ✅ | |
| `Event::IsTerminalEmulator()` | `FtxUIEvent.isTerminalEmulator` | ✅ | |
| `Event::TerminalEmulatorName()` | `FtxUIEvent.terminalEmulatorName` | ✅ | |
| `Event::TerminalEmulatorVersion()` | `FtxUIEvent.terminalEmulatorVersion` | ✅ | |
| `Event::IsTerminalCapabilities()` | `FtxUIEvent.isTerminalCapabilities` | ✅ | |
| `Event::TerminalCapabilities()` | `FtxUIEvent.terminalCapabilities` | ✅ | Returns `List<Int>` |
| `Event::DebugString()` | `FtxUIEvent.debugString` | ✅ | |

### Mouse

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Mouse::Button` enum | `MouseButton` enum | ✅ | All 8 values including wheel directions |
| `Mouse::Motion` enum | `MouseMotion` enum | ✅ | Released / Pressed / Moved |
| `Mouse::button` | `FtxUIEvent.mouseButton` | ✅ | |
| `Mouse::motion` | `FtxUIEvent.mouseMotion` | ✅ | |
| `Mouse::shift` | `FtxUIEvent.mouseShift` | ✅ | |
| `Mouse::meta` | `FtxUIEvent.mouseMeta` | ✅ | |
| `Mouse::control` | `FtxUIEvent.mouseControl` | ✅ | |
| `Mouse::x` | `FtxUIEvent.mouseX` | ✅ | |
| `Mouse::y` | `FtxUIEvent.mouseY` | ✅ | |

### Animation

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `animation::RequestAnimationFrame()` | `FtxUIApp.requestAnimationFrame()` | ⚠️ | Only via app instance; no global free function |
| `animation::Params` class | — | ❌ | No C API for Params/Animator |
| `animation::Params::duration()` | — | ❌ | |
| `animation::Animator(from, to, duration, easing, delay)` | — | ❌ | No C API |
| `animation::Animator::OnAnimation(Params&)` | — | ❌ | |
| `animation::Animator::to()` | — | ❌ | |
| `animation::easing::Linear` | `easing(EasingType.Linear)` | ✅ | Returns `EasingFunction = (Float)->Float` |
| `animation::easing::QuadraticIn/Out/InOut` | `easing(EasingType.QuadraticIn/Out/InOut)` | ✅ | |
| `animation::easing::CubicIn/Out/InOut` | `easing(EasingType.CubicIn/Out/InOut)` | ✅ | |
| `animation::easing::QuarticIn/Out/InOut` | `easing(EasingType.QuarticIn/Out/InOut)` | ✅ | |
| `animation::easing::QuinticIn/Out/InOut` | `easing(EasingType.QuinticIn/Out/InOut)` | ✅ | |
| `animation::easing::SineIn/Out/InOut` | `easing(EasingType.SineIn/Out/InOut)` | ✅ | |
| `animation::easing::CircularIn/Out/InOut` | `easing(EasingType.CircularIn/Out/InOut)` | ✅ | |
| `animation::easing::ExponentialIn/Out/InOut` | `easing(EasingType.ExponentialIn/Out/InOut)` | ✅ | |
| `animation::easing::ElasticIn/Out/InOut` | `easing(EasingType.ElasticIn/Out/InOut)` | ✅ | |
| `animation::easing::BackIn/Out/InOut` | `easing(EasingType.BackIn/Out/InOut)` | ✅ | |
| `animation::easing::BounceIn/Out/InOut` | `easing(EasingType.BounceIn/Out/InOut)` | ✅ | |
| `ComponentBase::OnAnimation(Params&)` override hook | — | ❌ | No C API for animation callback hook |

### ComponentBase

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `ComponentBase::Parent()` | — | ❌ | Not exposed |
| `ComponentBase::ChildAt(i)` | — | ❌ | Not exposed |
| `ComponentBase::ChildCount()` | — | ❌ | Not exposed |
| `ComponentBase::Index()` | — | ❌ | Not exposed |
| `ComponentBase::Add(Component)` | `ContainerComponent.add(component)` | ⚠️ | Only on `ContainerComponent`, not all components |
| `ComponentBase::Detach()` | — | ❌ | Not exposed |
| `ComponentBase::DetachAllChildren()` | — | ❌ | Not exposed |
| `ComponentBase::Render()` | `Component.render()` | ✅ | Extension function |
| `ComponentBase::OnRender()` override | `renderer(child, callback)` | ⚠️ | Via factory; no subclassing |
| `ComponentBase::OnEvent(Event)` override | `catchEvent(handler)` | ⚠️ | Via decorator; no subclassing |
| `ComponentBase::Focusable()` | — | ❌ | Not exposed |
| `ComponentBase::Active()` | — | ❌ | Not exposed |
| `ComponentBase::Focused()` | — | ❌ | Not exposed |
| `ComponentBase::SetActiveChild(...)` | — | ❌ | Not exposed |
| `ComponentBase::TakeFocus()` | — | ❌ | Not exposed |
| `ComponentBase::ActiveChild()` | — | ❌ | Not exposed |

### Container functions

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Container::Vertical(Components)` | `vertical(vararg components)` | ✅ | |
| `Container::Vertical(Components, int* selector)` | — | ❌ | Pre-wired selector form not exposed |
| `Container::Horizontal(Components)` | `horizontal(vararg components)` | ✅ | |
| `Container::Horizontal(Components, int* selector)` | — | ❌ | Pre-wired selector form not exposed |
| `Container::Tab(Components, int* selector)` | `tab(selected)` + `add()` | ⚠️ | Selector passed first; children added via `add()` |
| `Container::Stacked(Components)` | `stacked()` | ✅ | |

### Component factories

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Button(ButtonOption)` | — | ❌ | Options-only overload not exposed |
| `Button(label, onClick, options)` | `button(label, onClick, options)` | ✅ | |
| `Checkbox(CheckboxOption)` | — | ❌ | Options-only overload not exposed |
| `Checkbox(label, bool*, options)` | `checkbox(label, checked)` | ✅ | |
| `Input(InputOption)` | `input(options: InputOptions)` | ✅ | `InputOptions` class with content, placeholder, multiline, insert, cursorPosition, onChange, onEnter |
| `Input(content, options)` | `input(options: InputOptions)` | ✅ | Pass content via `InputOptions.content` |
| `Input(content, placeholder, options)` | `input(content, placeholder)` | ✅ | Simple form; full form via `InputOptions` |
| `Menu(MenuOption)` | — | ❌ | Options-only overload not exposed |
| `Menu(entries, selected, options)` | `menu(entries, selected)` | ⚠️ | `MenuOption` not fully exposed; no callbacks |
| `MenuEntry(MenuEntryOption)` | — | ❌ | Options-only overload not exposed |
| `MenuEntry(label, options)` | `menuEntry(label)` | ⚠️ | `transform` not exposed as Kotlin lambda |
| `Radiobox(RadioboxOption)` | — | ❌ | Options-only overload not exposed |
| `Radiobox(entries, selected, options)` | `radiobox(entries, selected)` | ⚠️ | `RadioboxOption` not exposed; no `on_change` |
| `Dropdown(entries, selected)` | `dropdown(entries, selected)` | ✅ | |
| `Dropdown(DropdownOption)` | `dropdownCustom(entries, selected, transform, entryTransform)` | ⚠️ | Partial; full `DropdownOption` struct not mirrored |
| `Toggle(entries, selected)` | `toggle(entries, selected)` | ✅ | |
| `Slider(SliderOption<T>)` | — | ❌ | Template options form not exposed |
| `Slider(label, int&, min, max, increment)` | `slider(label, value: IntState, min, max, increment)` | ✅ | |
| `Slider(label, float&, min, max, increment)` | `slider(label, value: FloatState, min, max, increment)` | ✅ | |
| `Slider(label, long&, min, max, increment)` | — | ❌ | `Long` slider not exposed |
| `Slider` for `int8/16/32/64`, `uint*`, `double` | — | ❌ | Only `Int` and `Float` exposed |
| `Slider` directional (value, min, max, inc, dir) | `slider(value, min, max, increment, direction)` | ✅ | Both `IntState` and `FloatState` forms |
| `ResizableSplit(ResizableSplitOption)` | `resizableSplit(main,back,size,dir,min,max,sep)` | ⚠️ | Options struct not directly exposed |
| `ResizableSplitLeft(main, back, size)` | `resizableSplitLeft(main, back, mainSize)` | ✅ | |
| `ResizableSplitRight(main, back, size)` | `resizableSplitRight(main, back, mainSize)` | ✅ | |
| `ResizableSplitTop(main, back, size)` | `resizableSplitTop(main, back, mainSize)` | ✅ | |
| `ResizableSplitBottom(main, back, size)` | `resizableSplitBottom(main, back, mainSize)` | ✅ | |
| `Renderer(child, fn)` | `renderer(child, callback)` | ✅ | |
| `Renderer(fn)` | `renderer(null, callback)` | ✅ | |
| `Renderer(fn<Element(bool focused)>)` | `focusableRenderer(callback)` | ✅ | |
| `Renderer(ElementDecorator)` | `Component.decorateRender(transform)` | ✅ | |
| `CatchEvent(child, fn)` | `Component.catchEvent(handler)` | ✅ | |
| `CatchEvent(fn)` — decorator form | `Component.catchEvent(handler)` | ✅ | |
| `Maybe(component, bool*)` | `maybe(child, show: BoolState)` | ✅ | |
| `Maybe(component, fn<bool()>)` | `maybe(child, predicate)` | ✅ | |
| `Maybe(bool*)` — decorator | `Component.maybe(show)` | ✅ | |
| `Maybe(fn<bool()>)` — decorator | `Component.maybe(predicate)` | ✅ | |
| `Modal(main, modal, bool*)` | `modal(main, modal, showModal)` | ✅ | |
| `Modal(modal, bool*)` — decorator | `Component.modal(modal, showModal)` | ✅ | |
| `Collapsible(label, child, show)` | `collapsible(label, child, show)` | ✅ | |
| `Hoverable(comp, bool*)` | `Component.hoverable(hover)` | ✅ | |
| `Hoverable(comp, onEnter, onLeave)` | `hoverable(comp, onEnter, onLeave)` / `Component.hoverable(onEnter, onLeave)` | ✅ | |
| `Hoverable(comp, onChange<bool>)` | `hoverable(comp, onChange)` / `Component.hoverable(onChange)` | ✅ | |
| `Hoverable(bool*)` — decorator | — | ❌ | Decorator form not exposed |
| `Hoverable(onEnter, onLeave)` — decorator | — | ❌ | |
| `Hoverable(onChange<bool>)` — decorator | — | ❌ | |
| `Window(WindowOptions)` | `windowComponent(options)` | ✅ | |

### Component options structs

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `EntryState.label` | `EntryState.label` | ✅ | |
| `EntryState.state` | `EntryState.state` | ✅ | |
| `EntryState.active` | `EntryState.active` | ✅ | |
| `EntryState.focused` | `EntryState.focused` | ✅ | |
| `EntryState.index` | `EntryState.index` | ✅ | |
| `ButtonOption::Simple()` | `ButtonOption.simple()` | ✅ | |
| `ButtonOption::Ascii()` | `ButtonOption.ascii()` | ✅ | |
| `ButtonOption::Border()` | `ButtonOption.border()` | ✅ | |
| `ButtonOption::Animated()` | `ButtonOption.animated()` | ✅ | |
| `ButtonOption::Animated(Color)` | `ButtonOption.animated(color)` | ✅ | |
| `ButtonOption::Animated(Color, Color)` | `ButtonOption.animated(bg, fg)` | ✅ | |
| `ButtonOption::Animated(Color, Color, Color, Color)` | `ButtonOption.animated(bg, fg, bgActive, fgActive)` | ✅ | |
| `ButtonOption.transform` (fn) | `ButtonOption.transform` | ✅ | |
| `ButtonOption.label` | — | ❌ | Passed to `button()` factory; not settable on options |
| `ButtonOption.on_click` | — | ❌ | Passed to `button()` factory; not settable on options |
| `ButtonOption.animated_colors` | — | ❌ | Raw `AnimatedColorsOption` not exposed |
| `UnderlineOption` | — | ❌ | Not exposed |
| `AnimatedColorOption` | — | ❌ | Not exposed |
| `AnimatedColorsOption` | — | ❌ | Only via `animatedMenuEntryColors()` helper function |
| `MenuEntryOption.transform` | — | ❌ | No Kotlin lambda form |
| `MenuEntryOption.animated_colors` | `menuEntry(label, animatedColors)` | ⚠️ | Via raw C value via `animatedMenuEntryColors()` only |
| `MenuOption::Vertical()` | — | ❌ | Full `MenuOption` not exposed |
| `MenuOption::Horizontal()` | — | ❌ | |
| `MenuOption::VerticalAnimated()` | — | ❌ | |
| `MenuOption::HorizontalAnimated()` | — | ❌ | |
| `MenuOption::Toggle()` | — | ❌ | |
| `MenuOption.on_change` | `menu(entries, selected, onChange)` | ✅ | Via `menu_with_callbacks` overload |
| `MenuOption.on_enter` | `menu(entries, selected, onEnter=)` | ✅ | Via `menu_with_callbacks` overload |
| `CheckboxOption::Simple()` | — | ❌ | No C API for styled checkbox options |
| `CheckboxOption.on_change` | `checkbox(label, checked, onChange)` | ✅ | Via `checkbox_with_change` overload |
| `InputOption::Default()` | `InputOptions()` | ✅ | Default `InputOptions` |
| `InputOption::Spacious()` | — | ❌ | No C API for spacious variant |
| `InputOption.multiline` | `InputOptions.multiline` | ✅ | |
| `InputOption.insert` | `InputOptions.insert` | ✅ | |
| `InputOption.on_change` | `InputOptions.onChange` | ✅ | |
| `InputOption.on_enter` | `InputOptions.onEnter` | ✅ | |
| `RadioboxOption.on_change` | `radiobox(entries, selected, onChange)` | ✅ | Via `radiobox_with_change` overload |
| `SliderOption<T>` | — | ❌ | No C API for templated slider options |
| `SliderOption.on_change` | `slider(value, min, max, increment, onChange)` | ✅ | Via `slider_int_with_change` / `slider_float_with_change` |
| `WindowOptions.inner` | `WindowOptions.inner` | ✅ | |
| `WindowOptions.title` | `WindowOptions.title` | ✅ | |
| `WindowOptions.left/top/width/height` | `WindowOptions.left/top/width/height` | ✅ | |
| `WindowOptions.resize_left/right/top/down` | — | ❌ | Resize handle control not exposed |
| `WindowOptions.render` | — | ❌ | Custom render function not exposed |
| `WindowRenderState` | — | ❌ | Not exposed |
| `DropdownOption.open` | — | ❌ | Not exposed |
| `DropdownOption.checkbox` | — | ❌ | Not exposed |
| `DropdownOption.radiobox` | — | ❌ | Not exposed |
| `DropdownOption.transform` | `dropdownCustom(..., transform)` | ⚠️ | Partial; only via `dropdownCustom` |
| `ResizableSplitOption.separator_func` | `resizableSplit(..., separator)` | ⚠️ | Exposed as lambda parameter; full options struct not mirrored |

---

## Util Module

| ftxui C++ | Kotlin (ftxui-kt) | Status | Notes |
|---|---|---|---|
| `Ref<T>` | `*State` / `KMutableProperty0<T>` | — | Intentional design difference; not a gap |
| `ConstRef<T>` | Kotlin value parameters | — | Intentional design difference |
| `StringRef` | `StringState` / `KMutableProperty0<String>` | — | Intentional design difference |
| `ConstStringRef` | `String` parameters | — | Intentional design difference |
| `ConstStringListRef` | `List<String>` parameters | — | Intentional design difference |
| `AutoReset<T>` | — | ❌ | No Kotlin equivalent; `try/finally` can substitute |
| `string_width(sv)` | — | — | Not needed; Kotlin handles string metrics |
| `Utf8ToGlyphs(sv)` | — | — | Not exposed; not needed in Kotlin |
| `CellToGlyphIndex(sv)` | — | — | Not exposed; not needed in Kotlin |
| `to_string(wstring_view)` | — | — | Not needed in Kotlin |
| `to_wstring(string_view)` | — | — | Not needed in Kotlin |

---

## Summary

Counts exclude items marked `—` (not applicable / intentional design differences).

| Module / Area | ✅ Implemented | ⚠️ Partial | ❌ Missing |
|---|---|---|---|
| Color | 16 | 1 | 1 |
| ColorInfo | 10 | 0 | 0 |
| Terminal | 9 | 0 | 3 |
| Element widgets | 37 | 4 | 5 |
| Element decorators | 40 | 2 | 3 |
| Canvas | 39 | 3 | 6 |
| FlexboxConfig | 7 | 0 | 6 |
| LinearGradient | 5 | 0 | 0 |
| Table & TableSelection | 34 | 1 | 1 |
| App (FtxUIApp) | 24 | 1 | 2 |
| Loop (FtxUILoop) | 5 | 0 | 0 |
| Event & Key constants | 31 | 0 | 3 |
| Mouse | 9 | 0 | 0 |
| Animation | 31 | 1 | 3 |
| ComponentBase | 2 | 2 | 9 |
| Container functions | 3 | 1 | 2 |
| Component factories | 30 | 4 | 11 |
| Component options structs | 23 | 3 | 17 |
| Util | 0 | 0 | 1 |
| **Total** | **355** | **23** | **73** |

### Top gaps by impact

1. **ComponentBase introspection** — `Parent`, `ChildAt`, `ChildCount`, `TakeFocus`, `Focused`, `Focusable`, `SetActiveChild` not accessible. No C API exists for these.
2. **Canvas Stylizer overloads** — `DrawPoint/Block/Line/Circle/Ellipse` with `Stylizer (Cell&)` callback not exposed; raw C callback form available but no Kotlin lambda wrapper.
3. **Component options styling** — `CheckboxOption::Simple()` / `Spacious`, `WindowOptions` resize handle flags, `WindowRenderState`, `MenuOption` animated/underline options have no C API backing.
4. **Animation Animator/Params** — `Animator`, `Params`, `OnAnimation` hook have no C API; only the easing functions are exposed.
5. **FlexboxConfig fluent builder** — `Set(Direction/Wrap/…)` / `SetGap()` have no C API equivalent; use the Kotlin data class constructor directly.
6. **Decorator-form Hoverable** — `ComponentDecorator` forms `Hoverable(bool*)`, `Hoverable(onEnter,onLeave)`, `Hoverable(onChange)` have no C API.
7. **`PostEvent` / `PostEventOrExecute`** — Events cannot be user-constructed; no C API for creating an event handle.
8. **`window(title, content, border)` element** — C API `ftxui_element_window` does not accept a border style parameter.

*Implemented in passes 1 & 2: all Loop, App factory/control, Mouse, Terminal, LinearGradient, Canvas, Table, easing functions, component callbacks (on_change/on_enter), InputOptions, Hoverable lambda forms, Maybe predicate, App terminal info, all FtxUIEvent fields.*

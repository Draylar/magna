# Magna

*Magna* is a library that assists with tools that break more than 1 block (Hammers, Excavators, etc). It powers the mods Vanilla Hammers and Vanilla Excavators.

### For Developers

#### Basics

Magna provides 2 base tools: `HammerItem` and `ExcavatorItem`. Registering an instance of one of these will offer a fully featured tool that is effective on dirt or stone.

```java
public class MyMod implements ModInitializer {

    public static final HammerItem EPIC_HAMMER = Registry.register(
        Registry.ITEM,
        new Identifier("mymod", "epic_hammer"),
        new HammerItem(
            ToolMaterials.DIAMOND,
            1,
            -2.8f,
            new Item.Settings()
        )
    );

    @Override
    public void onInitialize() {
  
    }
}
```

#### Curse of Gigantism

The *Curse of Gigantism* is a feature used by both *Vanilla Hammers* and *Vanilla Excavators*. If a relevant tool has the curse, it will gain +1 break radius,
but lose 80% speed. 

#### Custom Tool

If you want to create a custom tool with extended breaking mechanics, you can implement the `MagnaTool` interface on your `Item` class.
The only thing you'll have to do is override `getRadius(ItemStack stack)` and `playBreakEffects()`, which help define behavior related to your tool.

For more information, see implementation in `HammerItem` or `ExcavatorItem`.

#### Custom Breaking

If you don't want to use a tool for large breaking, you use the `BlockBreaker` `findPositions` method to gather positions relative to where a player is looking in a certain radius.

#### Events

Magna provides 2 custom events.

#### Existing Mod Integration

If you want to *only* add a Hammer or Excavator while Vanilla Hammers / Vanilla Excavators is installed,
you can call `Magna#isVanillaHammersInstalled()` or `Magna#isVanillaExcavatorsInstalled()`.

### For Users

Magna provides nothing out of the box for users, and developers should JIJ it. 

Magna *does* provide a unified set of config options to keep all your large-break-tools consistent. You can either edit the config file at `/run/magna.json`,
or edit the config through Mod Menu.

![](https://i.imgur.com/fk5iARa.png)

### License
Magna is licensed under CC0-1.0.

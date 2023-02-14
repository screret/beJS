// priority: 0


StartupEvents.registry('block', event => {
	event.create('example_block', 'entity' /*has to be here for the BE builder to work*/).material('wood').hardness(1.0).displayName('Example Block')
	.entity(builder => { // adds a BlockEntity onto this block
	    builder.ticker((level, pos, state, be) => { // a tick method, called on block entity tick
            if(!level.isClientSide) { // do (almost) anything you want here
                if(level.getBlockState(pos.above()) == 'minecraft:air'.defaultBlockState()) {
                    level.setBlock(pos.above(), 'minecraft:purple_glazed_terracotta'.defaultBlockState(), 3)
                } else {
                    level.setBlock(pos.above(), 'minecraft:air'.defaultBlockState(), 3)
                }
            }
    	}).saveCallback((level, pos, be, tag) => { // called on block entity save, don't see why you would ever need these tbf, but they're here
    	    tag.putInt("tagValueAa", be.getPersistentData().getInt('progress'))
    	}).loadCallback((level, pos, be, tag) => { // called on block entity load, same as above
            be.getPersistentData().putInt("progress", tag.getInt("tagValueAa"))
    	}).defaultValues(tag => { // adds a 'default' saved value, added on block entity creation (place etc) [1st param: CompoundTag consumer]
    	    tag.putInt('progress', 0)
    	    tag.putString('example_value_for_extra_saved_data', '0mG this iz Crazyyy')
    	}).addValidBlock('example_block') // adds a valid block this can attach to, useless in normal circumstances
    	.hasGui() // if ScreenJS is installed, marks this blockentity as having a GUI
    	.itemHandler(27) // adds a basic item handler to this block entity, use something like PowerfulJS for more advanced functionality
    	                 // [1st param: slot count]
    	.energyHandler(10000, 1000, 1000) // adds a basic FE handler, same as above [1st param: max energy, 2nd param: max input, 3rd param: max output]
    	.fluidHandler(1000, stack => true) // adds a basic fluid handler [1st param: max amount, 2nd param: fluid filter]
	})
})
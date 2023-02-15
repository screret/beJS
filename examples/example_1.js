// priority: 0


StartupEvents.registry('block', event => {
	event.create('example_block', 'entity' /*has to be here for the BE builder to work*/).material('wood').hardness(1.0).displayName('Example Block')
	.entity(builder => { // adds a BlockEntity onto this block
	    builder.ticker((level, pos, state, be) => { // a tick method, called on block entity tick
            if(!level.isClientSide) { // ALWAYS check side, the tick method is called on both CLIENT and SERVER
                if(level.getGameTime() % 20 === 0) {
                    if(level.getBlockState(pos.above()) === Blocks.AIR.defaultBlockState()) {
                        level.setBlock(pos.above(),Blocks.GLASS.defaultBlockState(), 3)
                    } else {
                        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3)
                    }
                }
                BE_LOGGER.info('aaa aaa')
            }
    	}).saveCallback((level, pos, be, tag) => { // called on BlockEntity save, don't see why you would ever need these tbf, but they're here
            tag.putInt("tagValueAa", be.getPersistentData().getInt('progress'))
        }).loadCallback((level, pos, be, tag) => { // called on BlockEntity load, same as above
              be.getPersistentData().putInt("progress", tag.getInt("tagValueAa"))
        }).defaultValues(tag => tag = { progress: 0, example_value_for_extra_saved_data: '0mG this iz Crazyyy'}) // adds a 'default' saved value, added on block entity creation (place etc)
                                                                                                                  // [1st param: CompoundTag consumer]
        .addValidBlock('example_block') // adds a valid block this can attach to, useless in normal circumstances (except if you want to attach to multible blocks)
        .hasGui() // if ScreenJS is installed, marks this blockentity as having a GUI, doesn't do anything otherwise
        .itemHandler(27) // adds a basic item handler to this block entity, use something like PowerfulJS for more advanced functionality
                         // [1st param: slot count]
        .energyHandler(10000, 1000, 1000) // adds a basic FE handler, same as above
                                          // [1st param: max energy, 2nd param: max input, 3rd param: max output]
        .fluidHandler(1000, stack => true) // adds a basic fluid handler
              	                                   // [1st param: max amount, 2nd param: fluid filter]
          	})
})

# Life Simulation

Project was developed as an assignment for the Object Oriented Programming course as AGH UST 2022/23. It is a result of cooperation with [Amazagni](https://github.com/Amazagni) and the process od development can be found [here](https://github.com/Amazagni/PO_Projekt1).

To run the app open project folder in terminal and type following lines

```
$ ./gradlew build
$ ./gradlew run
```

Or alternatively open project in IDE e.g. IntelliJ and run **World** class.

![](screenshots/mainView.png)

Main page of the application allows you to change the simulation parameters or choose one of the three prepared configurations.

Starting new application opens new window, where you can see the graphic interpretation of the ongoing simiulation with charts displaying most important data. Multiple simulations can be run simultanously.

![](screenshots/simulation.png)

You can pause the simulation and click on the animal you would like to see individual statistics of. It will be highlighted as blue. Red animals are the ones with dominant genome.

## Further Documentation

### Animals

Every animal can be expressed as a list of genes, that imply the way of them moving on the map. Every gene is a number from the range of **0-7**, which represent 8 directions animal can go e.g. north, south-west etc.

Additionally, they contain a set od statistics listed below
* Position
* Direction facing
* Current energy
* Index of the last gen that implied movement
* Birth day
* Age
* Number of grass eaten
* Number of children

### Simulation Rules

Simulation runs on few basics rules
* Everyday animals lose **Energy consumed on move** energy points
* Upon standing on the field with grass, animal eats it gaining **Energy gained by eating** energy points
* When multiple animals stand on the same grass

### Map


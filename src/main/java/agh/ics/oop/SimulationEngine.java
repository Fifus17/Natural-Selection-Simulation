package agh.ics.oop;

import java.util.*;

public class SimulationEngine implements Runnable {
    private List<Animal> animals = new ArrayList<>();
    private List<IAnimalMovementObserver> observers = new ArrayList<IAnimalMovementObserver>();
    public Animal highlightedAnimal = new Animal();
    public boolean showAnimalsWithDominantGenotype = false;
    public boolean isHighlighted = false;
    private int grassEnergyGain = 40;//będzie trzeba to przypisać w konstruktorze
    private int dailyEnergyLoss = 5;  // - || -
    private int startingNumberOfGrass = 10;// - || -
    private int dailyGrassGrowth = 20;// - || -
    private int startingEnergy = 120;//   // - || -
    private int energyUsedToCreateAnimal = 30; //- || - ilosc energi ktora rodzice łącznie tracą przy rozmnażaniu
    private int minEnergyToReproduce = 25; // - || - min energia zeby zwierze moglo sie rozmnazac ps trzeba zmienić tą nazwe xd
    private int genLength = 10; //Nie pamiętam ile jak długa miała być ta tablica -- to ma być parametr wejściowy

    //DOPISANE
    private int minNumberOfMutations = 3;
    private int maxNumberOfMutations = 5;
    private int averageLifeLength = 0;
    private int summaryLifeLength = 0;
    private int averageEnergyLevel = 0;
    private ArrayList<Integer> mostPopularGenotype = new ArrayList<>();
    private int mostPopularGenotypeCount = 0;
    public boolean isRunning = true;

    // Do statystyk
    private int day = 0;
    private int totalDead = 0;
    private int deadToday = 0;
    private int totalBorn = 0;
    private int bornToday = 0;

    public int getCurrentDayCount() { return this.day; }
    public int getTotalDeadNumber() { return this.totalDead; }
    public int getAverageEnergyLevel() { return this.averageEnergyLevel; }
    public int getAverageLifeLength() { return this.averageLifeLength; }


    //zmienne dotyczace wyboru mapy     |
    //(trzeba dopisać w konstruktorze)  V
    private Vector2d equatorLowerLeft;
    private Vector2d equatorUpperRight;
    private ArrayList<ToxicCorpsesField> corpses;
    private boolean earth = true;
    private boolean hellPortal = false;
    public boolean forestedEquators = true;
    private boolean toxicCorpses = false;
    private boolean randomMutation = true;
    private boolean slightlyChangedMutation = false;
    private boolean correctGenesOrder = true;
    private boolean slightlyChangedGenesOrder = false;
    //                                   ʌ
    //zmienne dotyczące wyboru mapy      |
    private EarthMap map;

    public SimulationEngine(
            EarthMap map, int startingNumberOfAnimals, int startingNumberOfGrass, int dailyGrassGrowth, int startingEnergy,
            int dailyEnergyLoss, int grassEnergyGain, int energyUsedToCreateAnimal, int minEnergyToReproduce, int genLength,
            int minNumberOfMutations, int maxNumberOfMutations, boolean earth, boolean forest, boolean slight, boolean following){
        this.map = map;
        this.startingNumberOfGrass = startingNumberOfGrass;
        if(this.startingNumberOfGrass < 0) this.startingNumberOfGrass = 0;
        this.dailyGrassGrowth = dailyGrassGrowth;
        if(this.dailyGrassGrowth < 0) this.dailyGrassGrowth = 0;
        this.startingEnergy = startingEnergy;
        if(this.startingEnergy < 1) this.startingEnergy = 1;
        this.dailyEnergyLoss = dailyEnergyLoss;
        if(this.dailyEnergyLoss < 0) this.dailyEnergyLoss = 1;
        this.grassEnergyGain = grassEnergyGain;
        if(this.grassEnergyGain < 0) this.grassEnergyGain = 0;
        this.energyUsedToCreateAnimal = energyUsedToCreateAnimal;
        if(this.energyUsedToCreateAnimal < 0) this.energyUsedToCreateAnimal = 0;
        this.minEnergyToReproduce = minEnergyToReproduce;
        if(this.minEnergyToReproduce < 0) this.minEnergyToReproduce = 0;
        this.genLength = genLength;
        if(this.genLength < 1) this.genLength = 1;
        this.maxNumberOfMutations = maxNumberOfMutations;
        if(this.maxNumberOfMutations > this.genLength) this.maxNumberOfMutations = this.genLength;
        if(this.maxNumberOfMutations < 0) this.maxNumberOfMutations = 0;
        this.minNumberOfMutations = minNumberOfMutations;
        if(this.minNumberOfMutations < 0) this.minNumberOfMutations = 0;
        if(this.minNumberOfMutations > this.maxNumberOfMutations) this.minNumberOfMutations = this.maxNumberOfMutations;
        for(int i = 0; i < startingNumberOfAnimals; i++){
            generateRandomAnimal();
        }
        this.earth = earth;
        this.hellPortal = !earth;
        this.forestedEquators = forest;
        this.toxicCorpses = !forest;
        this.slightlyChangedMutation = slight;
        this.randomMutation = !slight;
        this.correctGenesOrder = following;
        this.slightlyChangedGenesOrder = !following;
        //jeżeli jest to mapa z rownikiem, to tworzymy nowe lowerLeft i upperRight
        //około 20% mapy to rownik (jak w poleceniu)
        if(this.forestedEquators){
            Vector2d upperRight = this.map.getUpperRight();
            this.equatorLowerLeft = new Vector2d(0,(int)(0.4 * upperRight.y));
            this.equatorUpperRight = new Vector2d(upperRight.x, (int)(0.6* upperRight.y));
        }
        if(this.toxicCorpses){
            this.corpses = new ArrayList<>();
            for(int i = 0; i <= this.map.getUpperRight().x; i++){
                for(int j = 0; j <= this.map.getUpperRight().y; j++){
                    this.corpses.add(new ToxicCorpsesField(new Vector2d(i,j)));
                }
            }
        }
        generateRandomGrass(startingNumberOfGrass);
    }

    public Vector2d[] getEquatorCords() {
        if(this.forestedEquators) {
            Vector2d[] cords = new Vector2d[2];
            cords[0] = this.equatorLowerLeft;
            cords[1] = this.equatorUpperRight;
            return cords;
        }
        return null;
    }

    public EarthMap getMap() {return this.map;}

    public void addObserver(IAnimalMovementObserver application) {this.observers.add(application);}

    public boolean isForestTile(Vector2d position) {
        if (this.forestedEquators) {
            if(position.follows(this.equatorLowerLeft) && position.precedes(this.equatorUpperRight)) return true;
        }
        return false;
    }

    private void generateRandomAnimal(){
        int maxX = this.map.getUpperRight().x;
        int maxY = this.map.getUpperRight().y;
        //generuje x i y z przedzialu [0,max]
        //(max jest już poza naszą planszą)
        int newX = (int)(Math.random()*(maxX+1));
        int newY = (int)(Math.random()*(maxY+1));

        Vector2d position = new Vector2d(newX,newY);
        ArrayList<Integer> genes = new ArrayList<Integer>();
        for(int i = 0; i < this.genLength; i++){
            genes.add((int)(Math.random()*8));
        }
        Animal animal = new Animal(position,genes,this.startingEnergy, this.genLength);
        this.animals.add(animal);
        this.map.place(animal);

    }

    public int getAnimalsCount() { return this.animals.size(); }
    public int getGrassEnergyGain() { return grassEnergyGain; }

    //generowanie trawy tylko dla mapy z równikiemmm
    //drugą trzeba bedzie dopisać
    private void generateRandomGrass(int n){
        //generowanie dla mapy z rownikiem
        if(this.forestedEquators){
            int grassOnEquator = (int)(0.8*n);
            int index = 0;
            int maxIterations = 0;
            while (index < grassOnEquator){
                int newX = (int)(Math.random()*(this.equatorUpperRight.x + 1));
                int newY = (int)(Math.random()*(this.equatorUpperRight.y - this.equatorLowerLeft.y+1))+this.equatorLowerLeft.y;
                Vector2d newPosition = new Vector2d(newX,newY);
                if(!map.isGrassAt(newPosition)){
                    map.placeGrass(newPosition);
                    index += 1;
                }
                maxIterations += 1;
                //jesli losowaliśmy juz wiecej niz 100* grassOnEquator to breakujemy
                if(maxIterations > grassOnEquator * 100){
                    break;
                }
            }
            //nie znalezlismy wystarczajacej ilosci pol, sprawdzamy czy sa tam jeszcze jakies wolne
            if(maxIterations > grassOnEquator * 100){
                for(int i = equatorLowerLeft.x; i <= equatorUpperRight.x; i++){
                    for(int j = equatorLowerLeft.y; j <= equatorLowerLeft.y; j++){
                        if(!map.isGrassAt(new Vector2d(i,j))){
                            map.placeGrass(new Vector2d(i,j));
                            index += 1;
                            if(index == grassOnEquator)break;
                        }
                    }
                    if(index == grassOnEquator)break;
                }
            }
            maxIterations = 0;
            //generuje trawę poza równikiem
            while(index < n){
                int newX = (int)(Math.random()*(this.equatorUpperRight.x + 1));
                int newY;
                if(Math.random()<0.5){
                    newY = (int)(Math.random()*this.equatorLowerLeft.y);
                }
                else{
                    newY = (int)(Math.random()*(this.map.getUpperRight().y - this.equatorUpperRight.y + 1))+this.equatorUpperRight.y;
                }
                Vector2d newPosition = new Vector2d(newX,newY);
                if(!this.map.isGrassAt(newPosition)){
                    map.placeGrass(newPosition);
                    index += 1;
                    if(index == n)break;
                }
                maxIterations += 1;
                if(maxIterations > 100 * n){
                    break;
                }}
            Vector2d newPosition;
            if(index < n){
                for(int i = 0; i < equatorUpperRight.x; i++){
                    for(int j = 0; j < equatorUpperRight.y;j++){
                        newPosition = new Vector2d(i,j);
                        if(!map.isGrassAt(newPosition)){
                            map.placeGrass(newPosition);
                            index += 1;
                            if(index == n)return;
                        }

                    }
                }
            }

        }
        if(this.toxicCorpses){
            //corpses to tablica posortowana po ilosci trupow (rosnąco)
            //"lepsza część" to pierwsze 20% elementów tej tablicy
            int grassOnBetterFields = (int)(0.8 * n);
            int betterFields = (int)(0.2 * this.corpses.size());
            this.corpses.sort(new ToxicCorpsesComparator());
            int index = 0;
            int maxIterations = 0;
            while(index < grassOnBetterFields){
                int betterFieldId = (int)(Math.random()*(betterFields));
                if(!this.map.isGrassAt(this.corpses.get(betterFieldId).getPosition())){
                    this.map.placeGrass(this.corpses.get(betterFieldId).getPosition());
                    index += 1;
                }
                maxIterations += 1;
                if(maxIterations > grassOnBetterFields * 100)break;
            }
            maxIterations = 0;
            while(index < n){
                int worseFieldId = (int)(Math.random()*(this.corpses.size()-betterFields)+betterFields);
                if(!this.map.isGrassAt(this.corpses.get(worseFieldId).getPosition())){
                    this.map.placeGrass(this.corpses.get(worseFieldId).getPosition());
                    index += 1;
                }
                maxIterations += 1;
                if(maxIterations > n * 100)break;
            }
            if(index < n){
                for(int i = 0; i < corpses.size(); i++){
                    if(!this.map.isGrassAt(corpses.get(i).getPosition())){
                        this.map.placeGrass(corpses.get(i).getPosition());
                        index += 1;
                        if(index == n)return;
                    }
                }
            }

        }
    }


    private ArrayList<Integer> CreateChildGenes(Animal firstParent, Animal secondParent){
        int genesFromFirstParent = (int)(this.genLength *
                (firstParent.getEnergy()/(double)(firstParent.getEnergy()+secondParent.getEnergy())));

        ArrayList<Integer> childGenes = new ArrayList<>();
        boolean[] alreadyMutated = new boolean[genLength];

        int numberOfMutations = (int)(Math.random()*(this.maxNumberOfMutations - this.minNumberOfMutations + 1)) + minNumberOfMutations;

        int currentNumberOfMutations = 0;

        for(int j = 0; j < genesFromFirstParent; j++){
            childGenes.add(firstParent.getGenAt(j));
        }
        for(int j = genesFromFirstParent; j < this.genLength; j++){
            childGenes.add(secondParent.getGenAt(j));
        }
        int id = 0;
        while(true){
            if(currentNumberOfMutations == numberOfMutations)return childGenes;
            if(Math.random()<0.5 && !alreadyMutated[id]){
                int tmpGene = childGenes.get(id);
                //mutujemy gen
                if(this.slightlyChangedMutation){
                    if(Math.random()<0.5){
                        tmpGene = (tmpGene+1)%8;
                    }
                    else{
                        tmpGene -= 1;
                        if(tmpGene < 0)tmpGene = 7;
                    }
                }
                if(this.randomMutation){
                    tmpGene = (int)(Math.random()*8);
                }
                alreadyMutated[id] = true;
                childGenes.set(id,tmpGene);
                currentNumberOfMutations += 1;
                if(currentNumberOfMutations == numberOfMutations)return childGenes;
                id+=1;
                id = id%genLength;

            }

        }

    }
    private void updateMostPopularGenotype(){
        this.mostPopularGenotypeCount = 0;
        for(int i = 0; i < this.animals.size(); i++){
            int count = 1;
            ArrayList<Integer> currGenotype = this.animals.get(i).getGenes();
            for(int j = i + 1; j < this.animals.size(); j++){
                ArrayList<Integer> toCompare = this.animals.get(j).getGenes();
                boolean flag = true;
                for(int k = 0; k < this.genLength; k++){
                    flag = currGenotype.get(k) == toCompare.get(k);
                    if(!flag){
                        break;
                    }
                }
                if(flag)count += 1;
            }
            if(count > this.mostPopularGenotypeCount){
                this.mostPopularGenotypeCount = count;
                this.mostPopularGenotype = currGenotype;
            }
        }
    }

    public ArrayList<Integer> getMostCommonGenotype() { return this.mostPopularGenotype; }

    public int getMostPopularGenotypeCount() { return this.mostPopularGenotypeCount; }
    public int getDailyEnergyLoss() { return this.dailyEnergyLoss; }

    public void Start(){ this.isRunning = true; }
    public void Stop(){
        this.isRunning = false;
    }
    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println("Wystąpił błąd: "+ e);
            }
            while(this.isRunning) {
                this.totalBorn += this.bornToday;
                this.totalDead += this.deadToday;
                if(this.totalDead > 0){
                    this.averageLifeLength = this.summaryLifeLength/this.totalDead;
                }
                updateMostPopularGenotype();
//                System.out.println(this.map);
//                System.out.print("Dzień: ");
//                System.out.println(day);
//                System.out.print("Ilość żywych zwierząt: ");
//                System.out.println(this.animals.size());
//                System.out.print("Ilość urodzonych zwierząt: ");
//                System.out.println(this.totalBorn);
//                System.out.print("Dziś: ");
//                System.out.println(this.bornToday);
//                System.out.print("Ilość zmarłych zwierząt: ");
//                System.out.println(this.totalDead);
//                System.out.print("Dziś: ");
//                System.out.println(this.deadToday);
//                System.out.println("Średnia długość życia");
//                System.out.println(this.averageLifeLength);
//                System.out.println("Średni poziom energii");
//                System.out.println(this.averageEnergyLevel);
//                System.out.println("Najpopularniejszy genotyp: ");
//                System.out.println(this.mostPopularGenotype);
//                System.out.print("Wystąpień: ");
//                System.out.println(this.mostPopularGenotypeCount);

                this.bornToday = this.animals.size();
                this.day += 1;
                this.deadToday = 0;


                List<Animal> updatedAnimals = new ArrayList<>();//nowe animals dla engina i na tego podstawie uzupelni sie animals w mapie
                //List<Vector2d> positions = new ArrayList<>();// lista pozycji okupowanych przez zwierzęta
                int currGene;
                MapDirection newDirection;
                Vector2d newPosition;

                //czyscimy animals z mapy
                this.map.updateAnimals(new HashMap<>());

                //każde zwierze sie porusza
                for (Animal animal : this.animals) {

                    currGene = animal.getGen();
                    newDirection = animal.getDirection().changeDirection(currGene);
                    animal.updateDirection(newDirection);
                    newPosition = animal.getPosition().add(newDirection.toUnitVector());
                    animal.updatePosition(newPosition);
                    animal.updateEnergy(-this.dailyEnergyLoss);
                    animal.updateGeneIndex(this.correctGenesOrder,this.slightlyChangedGenesOrder);

                    //jesli zwierze wyszło poza granice mapy wysylamy je w odpowiednie miejsce
                    if(!(newPosition.follows(new Vector2d(0,0))&&newPosition.precedes(this.map.getUpperRight()))){
                        //sendBackToBorder(animal);
                        animal.sendBackToBorder(this.map,this.earth,this.hellPortal,this.energyUsedToCreateAnimal);
                    }
                    animal.updateEnergy(-dailyEnergyLoss);
                    animal.icrementAge();
                    if(animal.getEnergy()>=0){
                        updatedAnimals.add(animal);
                        this.map.place(animal);
                    }
                    //zwierze umiera
                    else{
                        if(this.toxicCorpses){
                            for(int i = 0; i < this.corpses.size(); i++){
                                if(this.corpses.get(i).getPosition().equals(newPosition)){
                                    ToxicCorpsesField tmp = corpses.get(i);
                                    tmp.icrementCorpses();
                                    this.corpses.set(i,tmp);
                                    break;


                                }
                            }
                        }

                        this.deadToday += 1;
                        this.summaryLifeLength += animal.getAge();
                        animal.updateDiedAt(this.day);
                    }

                }
                this.animals = updatedAnimals;

                Map<Vector2d, ArrayList<Animal>> mapAniamls = this.map.getAnimals();
                mapAniamls.forEach((position,animalList)->{
                    //zjadanie trawy
                    if(this.map.isGrassAt(position)){
                        Animal updatedAnimal = animalList.get(0);
                        updatedAnimal.updateEnergy(grassEnergyGain);
                        updatedAnimal.incrementGrassEaten();
                        animalList.set(0,updatedAnimal);
                        //this.map.updateAnimalsAt(position,animalList);
                        this.map.deleteGrassAt(position);
                    }
                    //rozmnażanie
                    int animalsOnPosition = animalList.size();
                    if(animalsOnPosition>=2){
                        for(int i = 0 ; i < animalsOnPosition -1; i+=2){
                            Animal firstParent = animalList.get(i);
                            Animal secondParent = animalList.get(i+1);
                            if(secondParent.getEnergy()<this.minEnergyToReproduce)break;
                            //50% szans na to że potomek dziedziczy lewą część od rodzica z wiekszą ilośćią energii
                            if(Math.random()<0.5){
                                firstParent = animalList.get(i+1);
                                secondParent = animalList.get(i);
                            }
                            ArrayList<Integer> childGenes = this.CreateChildGenes(firstParent,secondParent);

                            //zmniejszamy energie rodzicow;
                            int firstParentEnergyLoss = (int)(this.energyUsedToCreateAnimal *
                                    (firstParent.getEnergy()/(double)(firstParent.getEnergy()+secondParent.getEnergy())));
                            firstParent.updateEnergy(-firstParentEnergyLoss);
                            secondParent.updateEnergy(-(this.energyUsedToCreateAnimal - firstParentEnergyLoss));
                            firstParent.icrementNumberOfChildren();
                            secondParent.icrementNumberOfChildren();
                            //dodajemy nowe zwierze
                            Animal child = new Animal(position,childGenes,this.energyUsedToCreateAnimal, genLength);
                            animalList.add(child);
                            this.animals.add(child);
                        }

                    }
                    int summaryEnergy = 0;
                    for(int i = 0; i < animalsOnPosition; i++){
                        summaryEnergy += animalList.get(i).getEnergy();
                    }
                    this.averageEnergyLevel = 0;
                    if(animalList.size()!= 0)this.averageEnergyLevel = summaryEnergy / animals.size();
                });
                this.bornToday = this.animals.size() + this.deadToday - this.bornToday;
                generateRandomGrass(this.dailyGrassGrowth);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("Przerwano symulacje: "+ e);


                }

                // observers
                for (IAnimalMovementObserver observer: observers) {observer.animalMoved();}
            }}
    }
}

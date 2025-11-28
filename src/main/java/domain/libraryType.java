package domain;


public enum libraryType {
    CD (1 , 7 , 20) ,
    Book(2 , 28 , 10);

    private  int id ;
    private  int  overdue;
    private int price ;


    libraryType(int id , int overdue, int price) {
        this.id  = id;
        this.overdue = overdue;
        this.price = price;
    }
    public int getType(){ return id;}

    public static libraryType chooseType(int type){
        for(libraryType ty : libraryType.values()){
            if (ty.getType() == type ) return ty;
        }
        throw new IllegalArgumentException("This item: (" + type +") isn't valid :(");
    }
    public static libraryType fromStringType(String str) {
        return libraryType.valueOf(str.toUpperCase());
    }
}


 This demo app is modified from https://github.com/zkoss/zkessentials/tree/zk8-jpa and shows three different
 ways of implementing the same demo page by using 3 different design patterns.
 
 1. Classic MVC
    Pro: The easiest to implement and learn
    Con: The least flexible. The highest number of lines to write. UI components declared in 
         controllers so no clean separation of view and controller. Can't use @NotifyChange in Java 
         to update UI components in zul file. No data binding => Data change in UI component won't 
         update data of DTO/POJO/JPA automatically
    
 2. MVC embedding MVVM
    Pro: Better than MVC approach as MVVM features(e.g. data binding) is available in pages 
         embedded by the parent panel
    Con: Controller still has Con from 1 above
    
    * This demo also shows how to communicate between MVC and MVVM
 
 3. MVVM embedding MVVM
    Pro: The most flexible. View-Model has no knowledge of UI components -> clean separation of 
         concern. Data binding is availale in all pages. Least number of lines to implement.
         View-Model can fire events to update UI components in view layer automatically once
         data from View-Model (e.g. DTO/POJO/JPA/List) bound to UI components changes, e.g. 
         use @NotifyChange. 
    Con: More complex than MVC
    
    * This demo also shows how to communicate between MVVM and MVVM
    
 
 
 
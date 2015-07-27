package ekraft.verysimplerest.examples;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TodoItem {

  private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private String description;
  private String name;
  private Date due;


  public TodoItem() {

  }


  public TodoItem(final String description,
                  final String name,
                  final Date due) {

    this.description = description;
    this.name = name;
    this.due = due;
  }


  public TodoItem(final String description,
                  final String name,
                  final String due) throws ParseException {

    this(description, name, DATE_FORMAT.parse(due));
  }


  public TodoItem(String description,
                  String name) {

    this(description, name, new Date());
  }


  public String getDescription() {

    return description;
  }


  public String getName() {

    return name;
  }


  public Date getDue() {

    return due;
  }


  public void setDescription(String description) {

    this.description = description;
  }


  public void setName(String name) {

    this.name = name;
  }


  public void setDue(Date due) {

    this.due = due;
  }


  public boolean equals(Object other) {

    if (other == null) {
      return false;
    }

    if (!(other instanceof TodoItem)) {
      return false;
    }

    TodoItem todoItem = (TodoItem) other;

    return
      compare(description, todoItem.description) &&
      compare(name, todoItem.name) &&
      compare(due, todoItem.due);
  }


  private static boolean compare(Object object1,
                                 Object object2) {

    if (object1 == null) {
      return (object2 == null);
    }

    if (object2 == null) {
      return false;
    }

    return object1.equals(object2);
  }
}
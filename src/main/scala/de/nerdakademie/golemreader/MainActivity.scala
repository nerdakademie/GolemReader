package de.nerdakademie.golemreader

import org.scaloid.common._

/**
  * Created by xilent on 29.12.15.
  */
class MainActivity extends SActivity{

  onCreate{
    contentView = new SRelativeLayout {
      SListView().<<.fill.marginRight(3.dip).marginLeft(3.dip).>>
    }
  }

}

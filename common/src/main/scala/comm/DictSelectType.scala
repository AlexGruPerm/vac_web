package comm

sealed trait DictSelectType{
  def getHtml: String
}
case object SingleSelect extends DictSelectType{
  def getHtml: String = " "
}
case object MultiSelect extends DictSelectType{
  def getHtml: String = "multiple"
}

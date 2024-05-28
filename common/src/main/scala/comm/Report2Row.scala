package comm

final case class Report2Row(
                             rn: Int,
                             nyear: Int,
                             id_omsu: Int,
                             omsu_name: String,
                             id_voc_agent: Int,
                             org_name: String,
                             ls_error: String,
                             r_1_koef_totcnt_errcnt: String,
                             r_2_koef_totcnt_errcnt: String,
                             r_3_koef_totcnt_errcnt: String,
                             r_4_koef_totcnt_errcnt: String,
                             r_5_koef_totcnt_errcnt: String,
                             r_6_koef_totcnt_errcnt: String,
                             r_7_koef_totcnt_errcnt: String,
                             r_8_koef_totcnt_errcnt: String,
                             r_9_koef_totcnt_errcnt: String,
                             r_10_koef_totcnt_errcnt: String,
                             r_11_koef_totcnt_errcnt: String,
                             r_12_koef_totcnt_errcnt: String,
                             total: Int
                           )

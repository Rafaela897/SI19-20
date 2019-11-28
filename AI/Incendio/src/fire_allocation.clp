(deftemplate vehicle
    (slot score)
    (slot name)
    )
 
 (deftemplate fire
 	(slot name)
 )
 
 (deftemplate allocation
 	(slot firename)
 	(slot vehiclename)
 	)
   
(defrule allocate_vehicles
	?f <- (fire (name ?n))
	?v <- (vehicle (name ?n1) (score ?s1))
	(not (vehicle (score ?s2&:(< ?s2 ?s1))))
	=>
	(assert (allocation (firename ?n) (vehiclename ?n1)))
	)
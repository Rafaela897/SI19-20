(deftemplate vehicle
    (slot score)
    (slot name)
    )
 
 (deftemplate task
 	(slot name)
 )
 
 (deftemplate allocation
 	(slot taskname)
 	(slot vehiclename)
 	)
   
(defrule allocate_vehicles
	?f <- (task (name ?n))
	?v <- (vehicle (name ?n1) (score ?s1))
	(not (vehicle (score ?s2&:(< ?s2 ?s1))))
	=>
	(assert (allocation (taskname ?n) (vehiclename ?n1)))
	)
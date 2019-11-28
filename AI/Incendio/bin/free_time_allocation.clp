(deftemplate vehicle
    (slot water)
    (slot fuel)
    (slot maxFuel)
    (slot maxWater)
    )
 
 (deftemplate decision
 	(slot value)
 )
 

   
(defrule allocate_time
	?f <- (vehicle (fuel ?f) (water ?w) (maxFuel ?f1) (maxWater ?w1) )
	=>
	(if < (?w  ?w1) ) then  (assert (decision (value 0) ))
	else (if (< ?f ?f1) then (assert (decision (value 1))) else (assert (decision (value 3))))
	)